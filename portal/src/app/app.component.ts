import { Component, Injectable, Output } from '@angular/core';
import { MatSelectChange } from '@angular/material/select';
import { Observable } from 'rxjs';
import { Cluster } from './models/core.model';
import { AppStore } from './stores/app.store'

@Component({
  selector: 'app-root',
  templateUrl: './app.component.html',
  styleUrls: ['./app.component.css']
})
export class AppComponent {

  currentCluster?: Cluster

  constructor(
    private appStore: AppStore
  ) {
    this.appStore.clusters.subscribe(val => { if (val.length > 0) { this.currentCluster = val[0] } })
  }

  public get clusters(): Observable<Array<Cluster>> {
    return this.appStore.clusters
  }

  currentClusterChanged() {
    this.appStore.currentCluster.next(this.currentCluster!!)
  }

}
