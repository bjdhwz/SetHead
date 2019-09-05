package main.java.com.sethead;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.bukkit.inventory.meta.SkullMeta;

import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Base64;
import java.util.UUID;

public class SetHead implements CommandExecutor  {
    public static final String USAGE = "/sethead [player's name|player's id|Base64 value|textures.minecraft.net URL] [(optional) item's name]";

    public String[] getFromName(String name) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + name);
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            String id = json.get("id").getAsString();

            if (id != null) {
                return getFromId(id);
            }

            return null;
        } catch (Exception e) { // IOException
            return null;
        }
    }

    public String[] getFromId(String id) {
        try {
            URL url = new URL("https://sessionserver.mojang.com/session/minecraft/profile/" + id + "?unsigned=true"); // + "?unsigned=false");
            InputStreamReader reader = new InputStreamReader(url.openStream());
            JsonObject json = new JsonParser().parse(reader).getAsJsonObject();
            String name = json.get("name").getAsString();
            String value = json.get("properties").getAsJsonArray().get(0).getAsJsonObject().get("value").getAsString();
            //String signature = textureProperty.get("signature").getAsString();

            if (value != null) {
                return new String[] {id, name, value};
            }

            return null;
        } catch (Exception e) {
            return null;
        }
    }

    @SuppressWarnings("deprecation")
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player ) {
            Player player = (Player) sender;

            if (args.length == 0) {
                player.sendMessage(USAGE);
            }

            else if (args.length > 0) {
                ItemStack itemInHand = player.getInventory().getItemInMainHand();

                if (itemInHand.getType() == Material.PLAYER_HEAD
                        || itemInHand.getType() == Material.ZOMBIE_HEAD
                        || itemInHand.getType() == Material.CREEPER_HEAD
                        || itemInHand.getType() == Material.SKELETON_SKULL
                ) {
                    String input = args[0].replaceAll("[^A-Za-z0-9_=/:.+-]","");
                    String id = null;
                    String name = null;
                    String value = null;

                    if (input.length() == 64
                            || input.length() == 95
                            || input.length() == 102
                            || input.length() == 103) { // assume input is URL (various inputs: with http, https, domain without protocol and just the value)

                        if (input.length() == 64) {
                            input = "http://textures.minecraft.net/texture/" + input;
                        } else if (input.length() == 95) {
                            input = "http://" + input;
                        }

                        if (input.contains("://textures.minecraft.net/texture/")) {
                            URI url;

                            try {
                                url = new URI(input);
                                value = "{\"textures\":{\"SKIN\":{\"url\":\"" + url.toString() + "\"}}}";
                                value = Base64.getEncoder().encodeToString(value.getBytes());
                                id = "ffffffffffffffffffffffffffffffff";
                            } catch (URISyntaxException e) {
                                //player.sendMessage("Exception");
                            }
                        }

                    } else if (input.length() == 180 || input.length() == 348) { // assume input is Value
                        id = "ffffffffffffffffffffffffffffffff";
                        value = input;

                    } else if (input.length() == 32 || input.length() == 36) { // assume input is UUID
                        String[] props = getFromId(input.replaceAll("-", ""));

                        if (props != null) {
                            id = props[0];
                            //name = props[1]; // can be used to fake the "real" heads
                            value = props[2];
                        }

                    } else if (input.length() <= 16) { // assume input is Nickname
                        String[] props = getFromName(input);

                        if (props != null) {
                            id = props[0];
                            //name = props[1];
                            value = props[2];
                        }

                    }

                    if (value != null) {
                        ItemStack head = new ItemStack(Material.PLAYER_HEAD);

                        /*GameProfile skin502283474 = new GameProfile(UUID.fromString("e64b3386-879f-4317-8316-f83aa10620d7"), "skin502283474");
skin502283474.getProperties().put("textures", new Property("textures",
"eyJ0aW1lc3RhbXAiOjE1NjE2ODk4NTAxMjMsInByb2ZpbGVJZCI6ImU2NGIzMzg2ODc5ZjQzMTc4MzE2ZjgzYWExMDYyMGQ3IiwicHJvZmlsZU5hbWUiOiJCYWRCb25lczY5Iiwic2lnbmF0dXJlUmVxdWlyZWQiOnRydWUsInRleHR1cmVzIjp7IlNLSU4iOnsidXJsIjoiaHR0cDovL3RleHR1cmVzLm1pbmVjcmFmdC5uZXQvdGV4dHVyZS8xZWUzMTI2ZmYyYzM0M2RhNTI1ZWVmMmI5MzI3MmI5ZmVkMzYyNzNkMGVhMDhjMjYxNmI4MDAwOTk0OGFkNTdlIn19fQ==",
 "TgNywmetxtDHwXhnrhcIPo675XjJuFpioG9dU7D0RnmBbbQOF82TXaJ3+Y8yhOM3CV+WMqEjx8InGQsZaPk0KadzG6+l6GM+X+e4Gu4yfUKO8b/PVShYJiBd9/GoHJTQdAOsitAsCgTyVbXmo/cc7hg8+PqtEJ+N1OUtjw17PdiYJx00+vX18qOSfCjxUOjxDrOSfGVbb8tz8V68eOjWeMGXo3oQJx76u1D2P6kHozIpbsit3S/qK2U8tQmR5slAhF76swyi8JDiVn1uITrHc7PowK1Ih6jPvB406f+1w0upzFpb/4Wrz3PLczQSAsWEs9YwAqvf5lFl2f1AJlTb9pI8SwZB0wyHqF9llw3w93weKXc1LgMNYyg++Mt2laAYppjolHhshHDizJrXwZUuehB4FH2igwRPdMrsbpj5dICakNKadlrHyGITXYlYzm3Ky8VMNWKr8+5/sc+TzNEj82QusMF0VVK/UamWYaEOXFvgXWBf51FWccoB+UNhCTCc34kIJ7PJI8ki6HZMmBM+aEdBq93crTt/zXFAxvdMga3nIolhEIA1aEyAywYXVkDjLENahMFhOUQPXGFidX1x5SYBOaZiZNX7OOu/b8tPxD1tarn5x0Pc4Zumilmwn643jY8+8/9hbEZUGV5hgY5E8whpsrLnkb5pNC/W3dpQUns="
));*/

                        String nbt = "{SkullOwner:{Properties:{textures:[{Value:\"" + value + "\"}]}}}";

                        if (id != null) {
                            String msb = id.substring(0, 16);
                            String lsb = id.substring(16);
                            id = new UUID(Long.parseUnsignedLong(msb, 16), Long.parseUnsignedLong(lsb, 16)).toString(); // add dashes to UUID
                            nbt = "{SkullOwner:{Id:\"" + id + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}";
                            if (name != null) {
                                nbt = "{SkullOwner:{Id:\"" + id + "\",Name:\"" + name + "\",Properties:{textures:[{Value:\"" + value + "\"}]}}}";
                            }
                        }

                        Bukkit.getUnsafe().modifyItemStack(head, nbt);

                        SkullMeta meta = (SkullMeta) head.getItemMeta();
                        meta.setDisplayName("Decorative Head");

                        if (args.length == 2) {
                            meta.setDisplayName(args[1]);
                        }

                        head.setItemMeta(meta);
                        PlayerInventory inventory = player.getInventory();
                        inventory.setItemInMainHand(head);

                    } else {
                        player.sendMessage(USAGE);
                    }
                }
            }
        }

        return true;
    }
}