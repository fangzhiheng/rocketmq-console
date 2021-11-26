import { AfterViewInit, Component, Inject, Input, OnInit, ViewChild } from '@angular/core';
import { Broker, Cluster } from 'src/app/models/core.model';
import { AppService } from 'src/app/services/app.service';
import { BrokerService } from 'src/app/services/broker/broker.service';
import { AppStore } from 'src/app/stores/app.store';
import { MatTableDataSource } from '@angular/material/table';
import { MatPaginator } from '@angular/material/paginator';
import { map } from 'rxjs';

@Component({
  selector: 'app-broker',
  templateUrl: './broker.component.html',
  styleUrls: ['./broker.component.css']
})
export class BrokerComponent implements OnInit {
  displayedColumns: string[] = ['cluster', 'name'];
  brokers: MatTableDataSource<Broker> = new MatTableDataSource()

  @ViewChild(MatPaginator) paginator?: MatPaginator;
  constructor(
    private appStore: AppStore,
    private brokerService: BrokerService
  ) {
  }
  ngOnInit(): void {
    this.appStore.currentCluster.subscribe(val => {
      this.brokers.data = val.brokers!
      this.brokers.paginator = this.paginator!
    })
  }

}
