package de.hatoka.bubbles.human.internal.remote;

import static org.springframework.util.MimeTypeUtils.APPLICATION_JSON_VALUE;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import de.hatoka.bubbles.human.capi.business.HumanBO;
import de.hatoka.bubbles.human.capi.business.HumanBORepository;
import de.hatoka.bubbles.human.capi.business.HumanRef;
import de.hatoka.bubbles.human.capi.remote.HumanCreateRO;
import de.hatoka.bubbles.human.capi.remote.HumanRO;
import de.hatoka.common.capi.rest.RestControllerErrorSupport;
import de.hatoka.user.capi.business.UserRef;
import jakarta.ws.rs.QueryParam;

@RestController
@RequestMapping(value = HumanController.PATH_ROOT, produces = { APPLICATION_JSON_VALUE })
public class HumanController
{
    public static final String PATH_ROOT = "/humans";
    public static final String PATH_SUB_HUMAN = "/{humanid}";
    public static final String QUERY_USER_REF = "userRef";
    public static final String PATH_VAR_HUMANID= "humanid";
    public static final String PATH_HUMAN = PATH_ROOT + PATH_SUB_HUMAN;
    
    @Autowired
    private HumanBORepository humanRepository;
    @Autowired
    private HumanBO2RO humanBO2RO;

    @Autowired
    private RestControllerErrorSupport errorSupport;

    @ResponseStatus(HttpStatus.OK)
    @GetMapping
    public List<HumanRO> getbubbles(@QueryParam(QUERY_USER_REF) String userRef)
    {
        Collection<HumanBO> bubbles = humanRepository.getHumans(UserRef.localRef(userRef));
        return humanBO2RO.apply(bubbles);
    }

    @PutMapping(value = PATH_SUB_HUMAN, consumes = { APPLICATION_JSON_VALUE })
    @ResponseStatus(HttpStatus.CREATED)
    public void createHuman(@PathVariable(PATH_VAR_HUMANID) String humanID, @RequestBody HumanCreateRO input)
    {
        if (input.getUserRef() == null || input.getUserRef().isBlank())
        {
            errorSupport.throwBadRequestException("human.create.no_user", humanID);
        }
        UserRef userRef = UserRef.localRef(input.getUserRef());
        HumanRef humanRef = HumanRef.localRef(humanID);
        Optional<HumanBO> humanOpt = humanRepository.findHuman(humanRef);
        if (humanOpt.isPresent())
        {
            errorSupport.throwNotFoundException("found.human", humanRef.toString());
        }
        humanRepository.createHuman(humanRef, input.getName(), userRef);
    }

    @GetMapping(PATH_SUB_HUMAN)
    @ResponseStatus(HttpStatus.OK)
    public HumanRO getHuman(@PathVariable(PATH_VAR_HUMANID) String humanID)
    {
        HumanRef humanRef = HumanRef.localRef(humanID);
        Optional<HumanBO> humanOpt = humanRepository.findHuman(humanRef);
        if (!humanOpt.isPresent())
        {
            errorSupport.throwNotFoundException("notfound.human", humanRef.toString());
        }
        return humanBO2RO.apply(humanOpt.get());
    }

    @DeleteMapping(PATH_SUB_HUMAN)
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void deleteHuman(@PathVariable(PATH_VAR_HUMANID) String humanID)
    {
        HumanRef humanRef = HumanRef.localRef(humanID);
        Optional<HumanBO> humanOpt = humanRepository.findHuman(humanRef);
        if (humanOpt.isPresent())
        {
            humanOpt.get().remove();
        }
    }
}