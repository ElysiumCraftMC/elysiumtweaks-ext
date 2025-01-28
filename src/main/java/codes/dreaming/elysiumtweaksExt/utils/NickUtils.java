package codes.dreaming.elysiumtweaksExt.utils;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.model.user.UserManager;
import net.luckperms.api.node.types.MetaNode;

import javax.annotation.Nullable;

import java.util.UUID;

import static codes.dreaming.elysiumtweaksExt.ElysiumtweaksExt.COMPANION_MOD_ID;

public class NickUtils {
    public static final String LuckPermsMetaNicknameKey = COMPANION_MOD_ID + ":nickname";

    public static MetaNode buildNicknameMetaNode(String nickname) {
        return MetaNode.builder().key(LuckPermsMetaNicknameKey).value(nickname).build();
    }

    @Nullable
    public static String parseNickname(String userInput) {
        String[] nickParts = userInput.trim().split(" +");
        if (nickParts.length == 0) {
            return null;
        }

        // Compute joined word capitalized version
        // mr nick -> MrNick
        StringBuilder nicknameBuilder = new StringBuilder();
        for (String nickPart : nickParts) {
            nicknameBuilder.append(nickPart.substring(0, 1).toUpperCase()).append(nickPart.substring(1).toLowerCase());
        }

        String nickname = nicknameBuilder.toString();

        // check for any non letter characters
        for (char character : nickname.toCharArray()) {
            if (!Character.isLetter(character)) {
                return null;
            }
        }

        return nickname;
    }

    @Nullable
    public static String getLpNickname(UUID playerUUID) {
        LuckPerms lpProvider = LuckPermsProvider.get();
        UserManager lpUserManager = lpProvider.getUserManager();
        User lpUser = lpUserManager.getUser(playerUUID);
        return NickUtils.getLpNickname(lpUser);
    }

    @Nullable
    public static String getLpNickname(@Nullable User lpUser) {
        if (lpUser == null) return null;
        return lpUser.getCachedData().getMetaData().getMetaValue(LuckPermsMetaNicknameKey);
    }
}
