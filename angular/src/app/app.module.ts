import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { StoreModule } from '@ngrx/store';
import { StoreDevtoolsModule } from '@ngrx/store-devtools';
import { HttpClientModule } from '@angular/common/http';
import { FormsModule, ReactiveFormsModule} from '@angular/forms';
import { OAuthModule } from 'angular-oauth2-oidc';
import { Router } from '@angular/router';
import { AppComponent } from './shell/components/app.component';
import { environment } from '../environments/environment';
import { AppRoutingModule } from './app-routing.module';

import * as bubbles from './bubbles';
import * as humans from './humans';
import * as relations from './relations';
import * as oidc from './oidc';
import * as core from './core/service/store';
import { CompositeRoutingModule } from './composites/routing';

@NgModule({
  declarations: [
    AppComponent,
  ],
  imports: [
    BrowserModule,
    FormsModule, ReactiveFormsModule, HttpClientModule,
    OAuthModule.forRoot(),
    StoreModule.forRoot({
      oidcState : oidc.oidcReducer,
      serviceState : core.serviceReducer,
      relationsState: relations.relationReducer,
    }),
    bubbles.BubbleModule,
    humans.HumanModule,
    relations.RelationsModule,
    oidc.OIDCModule,
    core.ServiceModule,
    CompositeRoutingModule,
    AppRoutingModule,
    StoreDevtoolsModule.instrument({
      name: 'Bubbles',
      maxAge: 50,
      logOnly: environment.production
    }),
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
  constructor(router: Router) {
  }
}
