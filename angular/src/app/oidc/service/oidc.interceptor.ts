import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { HttpEvent, HttpEventType, HttpHandler, HttpInterceptor, HttpRequest } from '@angular/common/http';
import { OIDCState } from '../store/oidc-reducers';
import { Store } from '@ngrx/store';
import { Token, selectAccessToken, selectResources } from '../store';
import { OIDCFacade } from './oidc.facade';

@Injectable({ providedIn: 'root' })
export class OIDCInterceptor implements HttpInterceptor {
  access_token : Token | undefined = undefined;

  uri_prefixes : Set<string> = new Set<string>();
  constructor(
    private readonly store: Store<OIDCState>,
    private readonly oidcFacade: OIDCFacade
  ) {
    this.store.select(selectAccessToken).subscribe( t => {
      if (t !== undefined)
      {
        this.access_token = t;
        console.log("interceptor access token received", this.access_token, new Date(t.expires_in).toUTCString());
        if (this.access_token.expires_in < new Date().getTime())
        {
          oidcFacade.clearAccessToken();
        }
      }
    });
    this.store.select(selectResources).subscribe( r => {
      if (r !== undefined)
      {
        this.uri_prefixes = r;
        // console.log("access scopes received", this.uri_prefixes);
      }
    });
  }

  public intercept(req: HttpRequest<any>, next: HttpHandler): Observable<HttpEvent<any>> {
    let url = req.url.toLowerCase();
    // console.log("interceptor started", url);
    if (!this.isAuthorizationHeaderEmpty(req))
    {
      // console.log("interceptor ignored - Authorization header set", url);
      return next.handle(req);
    }
    // access token is invalid, but refresh token exists
    if (!this.isAccessTokenAvailable())
    {
      // this.oidcFacade.tryToGetAccessToken();
    }
    if (!this.isAccessTokenAvailable())
    {
      // console.log("interceptor ignored - Access token is empty", url);
      return next.handle(req);
    }
    if (! this.isUriMatching(req)) {
      // console.log("interceptor not matching", url);
      return next.handle(req);
    }
    // console.log("interceptor matched", url);
    let headers = req.headers.set('Authorization', 'Bearer ' + this.access_token?.token);
    req = req.clone({ headers });
    if (req !== undefined)
    {
      return next.handle(req);
    }
    else{
      // TODO doesn't work
      return next.handle(req).pipe(o => {
        o.subscribe(e => {
          console.log("status", e.type);
          if (e.type === HttpEventType.Response) {
            console.log("status", e.url, e.status);
            if (e.status == 401 || e.status == 403) {
              this.oidcFacade.clearAccessToken();
            }
          }
        });
        return o;
      });
    }
  }

  private isUriMatching(req: HttpRequest<any>):boolean {
    var matched = false;
    this.uri_prefixes.forEach(prefix => {
      if (req.url.toLowerCase().startsWith(prefix))
      {
        matched = true;
      }});
    return matched;
  }

  private isAccessTokenAvailable():boolean {
    return this.access_token !== undefined;
  }
  private isAuthorizationHeaderEmpty(req: HttpRequest<any>):boolean {
    return req.headers.get('Authorization') === undefined || req.headers.get('Authorization') === null;
  }
}
