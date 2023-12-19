import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { AuthGuard } from '../oidc/guard';
import * as composites from './components';

const routes: Routes = [
{
  path: 'bubbles',
  canLoad: [AuthGuard],
  canActivate: [AuthGuard],
  children: [
    { path: '',       pathMatch: 'full', component: composites.ListBubblesPage },
    { path: ':bubble', pathMatch: 'prefix',
      children: [
        { path: '',          pathMatch: 'full', component: composites.DetailBubblePage },
      ]
    }
  ]
},
{
  path: 'humans',
  canLoad: [AuthGuard],
  canActivate: [AuthGuard],
  children: [
    { path: '',       pathMatch: 'full', component: composites.ListHumansPage },
    { path: ':human', pathMatch: 'prefix',
      children: [
        { path: '',          pathMatch: 'full', component: composites.DetailHumanPage },
        { path: 'edit',      pathMatch: 'full', component: composites.EditHumanPage },
        { path: 'relations', pathMatch: 'full', component: composites.RelationsHumanPage },
      ]
    }
  ]
},
];

@NgModule({
  imports: [
    RouterModule.forChild(routes)
  ],
  exports: [
    RouterModule
  ]
})
export class CompositeRoutingModule {}


/*
Copyright Google LLC. All Rights Reserved.
Use of this source code is governed by an MIT-style license that
can be found in the LICENSE file at https://angular.io/license
*/