package de.hatoka.oauth.internal.remote;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.apache.logging.log4j.util.Strings;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.util.UriComponentsBuilder;

import de.hatoka.bubbles.bubble.internal.remote.BubbleController;
import de.hatoka.bubbles.human.internal.remote.HumanController;
import de.hatoka.common.capi.rest.RestControllerErrorSupport;
import de.hatoka.oauth.capi.business.TokenUsage;
import de.hatoka.oauth.capi.business.TokenUtils;
import de.hatoka.oauth.capi.remote.OAuthRefreshRO;
import de.hatoka.oauth.capi.remote.OAuthTokenResponse;
import de.hatoka.oauth.capi.remote.OAuthUserAuthenticationRO;
import de.hatoka.oidc.capi.business.IdentityProviderBO;
import de.hatoka.oidc.capi.business.IdentityProviderBORepository;
import de.hatoka.oidc.capi.business.IdentityProviderRef;
import de.hatoka.oidc.capi.event.OIDCUserTokenGeneratedEvent;
import de.hatoka.oidc.capi.remote.OIDCUserInfo;

@RestController
@RequestMapping(value = UserTokenController.PATH_ROOT, produces = { APPLICATION_JSON_VALUE })
public class UserTokenController
{
    public static final String BEARER_PREFIX = "bearer ";
    public static final String PATH_ROOT = "/auth/users";
    public static final String PATH_SUB_TOKEN = "/token";
    public static final String PATH_SUB_REFRESH = "/refresh";

    @Autowired
    private ApplicationEventPublisher applicationEventPublisher;
    @Autowired
    private IdentityProviderBORepository idpRepository;
    @Autowired
    private RestControllerErrorSupport errorSupport;
    @Autowired
    private TokenUtils tokenUtils;

    @PostMapping(value = PATH_SUB_TOKEN, consumes = { APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public OAuthTokenResponse generateTokenForUser(@RequestHeader(HttpHeaders.AUTHORIZATION) String bearerTokenHeader, @RequestBody OAuthUserAuthenticationRO input, UriComponentsBuilder uriBuilder)
    {
        Optional<IdentityProviderBO> opt = getIdentityProvider(input.getIdpRef());
        if (!opt.isPresent())
        {
            errorSupport.throwNotFoundException("idp.notfound", input.getIdpRef());
        }
        IdentityProviderBO idp = opt.get();
        if (!bearerTokenHeader.toLowerCase().startsWith(BEARER_PREFIX))
        {
            errorSupport.throwNotFoundException("idp.bearer.notfound", bearerTokenHeader);
        }
        String bearerToken = bearerTokenHeader.substring(BEARER_PREFIX.length()).trim();
        if (TokenUsage.id != input.getType() && TokenUsage.refresh != input.getType())
        {
            errorSupport.throwNotFoundException("idp.bearer.usage.notfound", bearerToken);
        }
        OAuthTokenResponse result;
        if (TokenUsage.refresh == input.getType())
        {
            result = tokenUtils.createTokenFromRefreshToken(bearerToken);
        }
        else
        {
            OIDCUserInfo userInfo = idp.getUserInfo(bearerToken);
            result = tokenUtils.createTokenForSubject(userInfo.getSubject());
            result.setScope(getScopes(uriBuilder));
            applicationEventPublisher.publishEvent(new OIDCUserTokenGeneratedEvent(this, userInfo));
        }
        return result;
    }

    /**
     * Client like to have space separated list of URI to define assignment from uri to token
     * @param uriBuilder
     * @return
     */
    private String getScopes(UriComponentsBuilder uriBuilder)
    {
        List<String> uris = new ArrayList<>();
        uris.add(uriBuilder.replaceQuery(null).replacePath(BubbleController.PATH_ROOT).build().toUri().toString());
        uris.add(uriBuilder.replaceQuery(null).replacePath(HumanController.PATH_ROOT).build().toUri().toString());
        return Strings.join(uris, ' ');
    }

    @PostMapping(value = PATH_SUB_REFRESH, consumes = { APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.OK)
    public OAuthTokenResponse createTokenFromRefresh(@RequestBody OAuthRefreshRO input)
    {
        return tokenUtils.createTokenFromRefreshToken(input.getRefreshToken());
    }

    private Optional<IdentityProviderBO> getIdentityProvider(String idpLocalRefString)
    {
        IdentityProviderRef idpRef = IdentityProviderRef.valueOfLocal(idpLocalRefString);
        return idpRepository.findIdentityProvider(idpRef);
    }
}
