package com.example.demo;

import lombok.Getter;
import lombok.SneakyThrows;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpRequestDecorator;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.http.server.reactive.ServerHttpResponseDecorator;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.ServerWebExchangeDecorator;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.nio.charset.Charset;
import java.time.OffsetDateTime;

@Component
public class DemoWebFilter implements WebFilter , Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange serverWebExchange,
                             WebFilterChain webFilterChain) {

        serverWebExchange.getResponse()
                .getHeaders().add("web-filter", "web-filter-test");
        //https://developpaper.com/question/how-to-modify-the-request-parameters-of-multipart-form-data-format-in-spring-cloud-gateway/

        ServerHttpRequestDecorator decorator = new CachingServerHttpRequestDecorator(serverWebExchange.getRequest());

        return webFilterChain.filter(
                serverWebExchange.mutate().request(decorator).build()
        );
    }

    @Override
    public int getOrder() {
        return 1;
    }


    public static class PartnerServerWebExchangeDecorator extends ServerWebExchangeDecorator {

        private final ServerHttpRequestDecorator requestDecorator;
        private final ServerHttpResponseDecorator responseDecorator;

        public PartnerServerWebExchangeDecorator(ServerWebExchange delegate) {
            super(delegate);
            this.requestDecorator = new CachingServerHttpRequestDecorator(delegate.getRequest());
            this.responseDecorator = new ServerHttpResponseDecorator(delegate.getResponse());
        }

        @Override
        public ServerHttpRequest getRequest() {
            return requestDecorator;
        }

        @Override
        public ServerHttpResponse getResponse() {
            return responseDecorator;
        }

    }

    public static class CachingServerHttpRequestDecorator extends ServerHttpRequestDecorator {

        @Getter
        private final OffsetDateTime timestamp = OffsetDateTime.now();
        private final StringBuilder cachedBody = new StringBuilder();
        private Charset UTF_8 = Charset.forName("UTF-8");

        CachingServerHttpRequestDecorator(ServerHttpRequest delegate) {
            super(delegate);
        }

        @Override
        public Flux<DataBuffer> getBody() {
            return super.getBody().doOnNext(this::cache)
                    .doOnComplete(
                            ()-> System.out.println("onComplete: " + getCachedBody())
                    );
        }

        @SneakyThrows
        private void cache(DataBuffer buffer) {
            System.out.println("cache: " + buffer);
            cachedBody.append(UTF_8.decode(buffer.asByteBuffer())
                    .toString());
        }

        public String getCachedBody() {
            return cachedBody.toString();
        }
    }

    ///////

}
