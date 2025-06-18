package mods.Hileb.niceboat;

import net.minecraftforge.fml.relauncher.CoreModManager;
import net.minecraftforge.fml.relauncher.IFMLLoadingPlugin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nullable;
import java.io.File;
import java.util.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.ModMetadata;
import net.minecraftforge.fml.common.versioning.VersionParser;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.objectweb.asm.*;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;


@IFMLLoadingPlugin.Name(Tags.MOD_ID)
public class NiceBoat implements IFMLLoadingPlugin {

    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                "mods.Hileb.niceboat.NiceBoat&Transformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return "mods.Hileb.niceboat.NiceBoat&Container";
    }

    @Nullable
    @Override
    public String getSetupClass() {
        return null;
    }

    @Override
    public void injectData(Map<String, Object> data) {

    }

    @Override
    public String getAccessTransformerClass() {
        return null;
    }

    public static class Transformer implements IClassTransformer {
        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            if (basicClass != null && "".equals(transformedName)) {
                ClassWriter classWriter = new ClassWriter(0);
                new ClassReader(basicClass).accept(new ClassVisitor(Opcodes.ASM9, classWriter) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        if("func_184232_k".equals(name) || "updatePassenger".equals(name)) {
                            return new MethodVisitor(Opcodes.ASM9, super.visitMethod(access, name, descriptor, signature, exceptions)) {
                                @Override
                                public void visitCode() {
                                    super.visitCode();
                                    this.visitVarInsn(Opcodes.ALOAD, 0);
                                    this.visitVarInsn(Opcodes.ALOAD, 1);
                                    this.visitMethodInsn(Opcodes.INVOKESTATE, "mods/Hileb/niceboat/NiceBoat$Hook", "onUpdate", "(Lnet/minecraft/entity/item/EntityBoat;Lnet/minecraft/entity/Entity;)V", false);
                                }

                                @Override
                                public void visitFieldInsn(int opcode, String owner, String name, String descriptor) {
                                    if ("field_70177_z".equals(name) || "rotationYaw".equals(name)) {
                                        if (opcode == Opcodes.PUTFIELD) {
                                            visitInsn(Opcodes.POP2);
                                            return;
                                       }
                                   }
                                   visitFieldInsn(opcode, owner, name, descriptor);
                                }
                            };
                        }
                        return super.visitMethod(access, name, descriptor, signature, exceptions);
                    };
                }, 0);

                return classWriter.toByteArray();
            } else return basicClass;
        }
    }

    public class Container extends DummyModContainer {

        public Container(){
            super(decodeMcModInfo(Container.class.getResourceAsStream("mcmod.info")).get(Tags.MOD_ID));
        }

        @Override
        public boolean registerBus(EventBus bus, LoadController controller) {
            return true;
        }

        public static Map<String, ModMetadata> decodeMcModInfo(InputStream stream) {
            JsonElement element = new JsonParser().parse(new InputStreamReader(stream));
            if (element instanceof JsonArray array) {
                Map<String, ModMetadata> map = HashMap.newHashMap(array.size());
                for (JsonElement element1 : array) {
                    ModMetadata modMetadata = decodeMetaData(element);
                    map.put(modMetadata.modId, modMetadata);
                }
                return map;
            } else {
                ModMetadata modMetadata = decodeMetaData(element);
                return Map.of(modMetadata.modId, modMetadata);
            }
        }

        @SuppressWarnings("all")
        public static ModMetadata decodeMetaData(JsonElement jsonElement){
            JsonObject json = jsonElement.getAsJsonObject();
            ModMetadata metadata = new ModMetadata();

            //basic message
            metadata.modId = json.get("modid").getAsString();
            metadata.name = json.get("name").getAsString();

            //optional message
            if (json.has("description")) metadata.description = json.get("description").getAsString();
            if (json.has("credits")) metadata.credits = json.get("credits").getAsString();
            if (json.has("url")) metadata.url = json.get("url").getAsString();
            if (json.has("updateJSON")) metadata.updateJSON = json.get("updateJSON").getAsString();
            if (json.has("logoFile")) metadata.logoFile = json.get("logoFile").getAsString();
            if (json.has("version")) metadata.version = json.get("version").getAsString();
            if (json.has("parent")) metadata.parent = json.get("parent").getAsString();
            if (json.has("useDependencyInformation")) metadata.useDependencyInformation = json.get("useDependencyInformation").getAsBoolean();
            if (metadata.useDependencyInformation){
                if (json.has("requiredMods")){
                    for(JsonElement element : json.getAsJsonArray("requiredMods")){
                        metadata.requiredMods.add(VersionParser.parseVersionReference(element.getAsString()));
                    }
                }
                if (json.has("dependencies")){
                    for(JsonElement element : json.getAsJsonArray("dependencies")){
                        metadata.dependencies.add(VersionParser.parseVersionReference(element.getAsString()));
                    }
                }
                if (json.has("dependants")){
                    for(JsonElement element : json.getAsJsonArray("dependants")){
                        metadata.dependants.add(VersionParser.parseVersionReference(element.getAsString()));
                    }
                }
            }
            if (json.has("authorList")){
                for(JsonElement element : json.getAsJsonArray("authorList")){
                    metadata.authorList.add(element.getAsString());
                }
            }
            if (json.has("screenshots")){ // this field was never used
                JsonArray array = json.getAsJsonArray("screenshots");
                int size = array.size();
                String[] screenshots = new String[size];
                for (int i = 0; i < size; i++){
                    screenshots[i] = array.get(i).getAsString();
                }
                metadata.screenshots = screenshots;
            }else metadata.screenshots = new String[0];
            if (json.has("updateUrl")){ // this field is out of date
                metadata.updateUrl = json.get("updateUrl").getAsString();
            }

            return metadata;
        }
    }

    public static class Hook {
        public static void onUpdate(EntityBoat boat, Entity passenger) {
            if (boat.isPassenger(passenger) && boat.getControllingPassenger() == passenger) {
                if (boat.getPaddleState(0) && boat.getPaddleState(1)) {
                    float angle = MathHelper.wrapDegrees(boat.rotationYaw - passenger.rotationYaw);
                    if (angle < 22.5f && angle >= 0.01f){
                        boat.rotationYaw = boat.rotationYaw - (angle/20 + 0.01f);
                    } else if (angle < 0.01f && angle >= -0.01f) {
                        boat.rotationYaw = boat.rotationYaw - angle;
                    } else if (angle < -0.01f && angle > -22.5f) {
                        boat.rotationYaw = boat.rotationYaw - (angle/20 - 0.01f);
                    }
                }
            }
        }
    }

}
