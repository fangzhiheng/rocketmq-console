export class Cluster {
  name?: string
  brokers?: Array<Broker>
}

export interface Broker {
  name: string
  address: Map<number, string>
  cluster: string
  config: any // TODO
  status: any // TODO
}

export interface Group {
  name: string
}

export interface Queue {
  broker: string,
  read: number,
  write: number,
  permission: number
}

enum TopicType {
  USER,
  SYSTEM
}

export interface Topic {
  name: string
  brokers: Array<Broker>
  queues: Array<Queue>
  groups: Array<Group>
  type: TopicType
  order: Map<string, number>
  permission: number
}

export interface Producer {
}

export interface Consumer {
}

export interface Client {
}

export interface Message {
}
