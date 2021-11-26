import { Injectable } from "@angular/core";
import { BehaviorSubject, Observable, Subject } from "rxjs";
import { Cluster } from "../models/core.model"
import { AppService } from "../services/app.service";

@Injectable({
    providedIn: 'root'
})
export class AppStore {
    readonly currentCluster: Subject<Cluster> = new Subject()
    readonly clusters: BehaviorSubject<Array<Cluster>> = new BehaviorSubject([] as Array<Cluster>)
    constructor(
        private appService: AppService
    ) {
        appService.findClusters()
            .subscribe(val => {
                this.clusters.next(val)
            })
        this.clusters.subscribe(val => {
            if (val.length > 0) {
                this.currentCluster.next(val[0])
            }
        })
    }
}