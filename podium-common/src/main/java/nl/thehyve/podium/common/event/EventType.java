package nl.thehyve.podium.common.event;

import java.util.Arrays;
import java.util.Optional;

public enum EventType {
    Status_Change,
    Authentication,
    Authentication_Failure,
    Authentication_Success,
    Authentication_Switch,
    Authorization,
    Authorization_Failed,
    Authorization_Success,
    Registration,
    Verified_Registration,
    Update_User,
    Update_UserPassword,
    Unlock_Account,
    Delete_User,
    Reset_UserPassword_Request,
    User_Activated,
    Send_ActivationKey;

    public static EventType fromName(String name) {
        Optional<EventType> optional = Arrays.stream(EventType.values()).filter(value ->
            value.toString().toUpperCase().equals(name.toUpperCase())
        ).findFirst();
        if (!optional.isPresent()) {
            throw new RuntimeException("Event type not recognised: " + name);
        }
        return optional.get();
    }

}
