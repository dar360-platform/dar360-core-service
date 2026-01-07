package ae.dar360.user.client.feign;

import ae.dar360.user.client.feign.response.ProductTypeDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

import java.util.List;

@FeignClient(value = "ProductFeign", url = "${path.url.product}")
public interface ProductServiceFeign {

    @GetMapping(path = "/master-data/product-types",
            consumes = MediaType.APPLICATION_JSON_VALUE,
            produces = MediaType.APPLICATION_JSON_VALUE)
    List<ProductTypeDto> getProductTypes(@RequestHeader("Authorization") String authorization);

}

