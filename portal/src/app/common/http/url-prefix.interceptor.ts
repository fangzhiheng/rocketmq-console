import {Inject, Injectable} from '@angular/core';
import {
  HttpRequest,
  HttpHandler,
  HttpEvent,
  HttpInterceptor
} from '@angular/common/http';
import {Observable} from 'rxjs';

@Injectable()
export class UrlPrefixInterceptor implements HttpInterceptor {

  constructor(
    @Inject('URL_PREFIX') private urlPrefix: string
  ) {

  }

  intercept(request: HttpRequest<unknown>, next: HttpHandler): Observable<HttpEvent<unknown>> {
    console.log(`filtered! ${request.url}`)
    return request.url.startsWith(this.urlPrefix) ?
      next.handle(request) :
      next.handle(request.clone({url: `${this.urlPrefix}/${request.url}`}));
  }
}
