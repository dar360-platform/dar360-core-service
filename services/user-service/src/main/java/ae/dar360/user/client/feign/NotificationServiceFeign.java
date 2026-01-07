package ae.dar360.user.client.feign;

import ae.dar360.user.dto.NotifyBodyDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(value = "NotificationServiceFeign", url = "${path.url.notification}")
public interface NotificationServiceFeign {
    @PostMapping(value = "/email/notify")
    void emailNotify(@RequestBody NotifyBodyDto notifyBody);
}

