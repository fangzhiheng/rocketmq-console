import { TestBed } from '@angular/core/testing';

import { UrlPrefixInterceptor } from './url-prefix.interceptor';

describe('UrlPrefixInterceptor', () => {
  beforeEach(() => TestBed.configureTestingModule({
    providers: [
      UrlPrefixInterceptor
      ]
  }));

  it('should be created', () => {
    const interceptor: UrlPrefixInterceptor = TestBed.inject(UrlPrefixInterceptor);
    expect(interceptor).toBeTruthy();
  });
});
