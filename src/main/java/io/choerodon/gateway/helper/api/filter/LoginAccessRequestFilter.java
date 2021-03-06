package io.choerodon.gateway.helper.api.filter;

import io.choerodon.gateway.helper.domain.RequestContext;
import io.choerodon.gateway.helper.domain.CheckState;
import org.springframework.stereotype.Component;

/**
 * loginAccess请求的权限校验
 */
@Component
public class LoginAccessRequestFilter implements HelperFilter {

    @Override
    public int filterOrder() {
        return 60;
    }

    @Override
    public boolean shouldFilter(RequestContext context) {
        return context.getPermission().getLoginAccess() && context.getCustomUserDetails() != null;
    }

    @Override
    public boolean run(RequestContext context) {
        context.response.setStatus(CheckState.SUCCESS_LOGIN_ACCESS);
        context.response.setMessage("Have access to this 'loginAccess' interface, permission: " + context.getPermission());
        return false;
    }
}
