import { Injectable } from '@angular/core';
import { HttpClient } from "@angular/common/http";
import { Cluster } from "../models/core.model";
import { BehaviorSubject, Observable, Subject } from "rxjs";

@Injectable({
  providedIn: 'root'
})
export class AppService {
  constructor(
    private http: HttpClient,
  ) {
  }

  findClusters(): Observable<Array<Cluster>> {
    return this.http.get<Array<Cluster>>('/cluster')
  }
}
