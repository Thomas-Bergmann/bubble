import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { map } from 'rxjs/operators';

import { Bubble } from 'src/app/bubbles/store';
import { ApiService } from 'src/app/core/service';

interface BubbleDataRO {
  name : string;
  userRef : string;
}

interface BubbleCreateRO extends BubbleDataRO {
  userRef : string;
}

interface BubbleRO {
  refLocal : string;
  refGlobal : string;
  resourceURI: string;
  data : BubbleDataRO;
}

@Injectable({ providedIn: 'root' })
export class BubbleService {
  constructor(private apiService: ApiService) {}

  getBubbles(userRef:string): Observable<Bubble[]> {
    return this.apiService
      .get<BubbleRO[]>(`/bubbles?userRef=${userRef}`)
      .pipe(map(ros => this.convertListBubbleRO(ros)));
  }

  addBubble(externalID: string, data : BubbleCreateRO): Observable<{}> {
    return this.apiService
      .put(`/bubbles/${externalID}`, data);
  }

  deleteBubble(bubble : Bubble): Observable<{}> {
    return this.apiService.delete(bubble.resourceURI);
  }

  convertListBubbleRO(ros : BubbleRO[]): Bubble[]
  {
    return ros.map(ro => convertBubbleRO(ro));
  }
}

function convertBubbleRO(ro : BubbleRO): Bubble
{
  return new Bubble().init(ro.resourceURI, ro.refLocal, ro.data.name);
}
