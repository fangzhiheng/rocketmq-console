import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';
import { BrokerComponent } from './layouts/broker/broker/broker.component';
import { TopicComponent } from './layouts/topic/topic/topic.component';

const routes: Routes = [
  {
    path: '',
    redirectTo: '/broker',
    pathMatch: 'full'
  },
  {
    path: '',
    children: [
      {
        path: 'broker',
        component: BrokerComponent
      },
      {
        path: 'topic',
        component: TopicComponent
      },
      {
        path: 'producer',
        component: TopicComponent
      },
      {
        path: 'consumer',
        component: TopicComponent
      },
      {
        path: 'message',
        component: TopicComponent
      }
    ]
  }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
