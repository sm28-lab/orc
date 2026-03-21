package dev.sorn.orc.api;

import java.net.URI;

public interface HttpClient<REQ, RES> {

    Result<RES> get(URI uri);

    Result<RES> post(URI uri, REQ body);

}
