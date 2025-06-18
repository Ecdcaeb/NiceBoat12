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
import com.google.common.eventbus.EventBus;
import net.minecraftforge.fml.common.DummyModContainer;
import net.minecraftforge.fml.common.FMLCommonHandler;
import net.minecraftforge.fml.common.ICrashCallable;
import net.minecraftforge.fml.common.LoadController;
import net.minecraftforge.fml.common.ModMetadata;

import java.io.InputStream;
import java.io.InputStreamReader;

import org.objectweb.asm.*;

import org.apache.commons.lang3.SystemUtils;

import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityBoat;
import net.minecraft.util.math.MathHelper;


@IFMLLoadingPlugin.Name(Tags.MOD_ID)
public class NiceBoat implements IFMLLoadingPlugin {

    public static final int ASM_API = SystemUtils.IS_JAVA_1_8 ? (5 << 16 | 0 << 8) : (9 << 16 | 0 << 8); // ASM5 : ASM9
    public static final Logger LOGGER = LogManager.getLogger(Tags.MOD_NAME);

    @Override
    public String[] getASMTransformerClass() {
        return new String[]{
                "mods.Hileb.niceboat.NiceBoat$Transformer"
        };
    }

    @Override
    public String getModContainerClass() {
        return "mods.Hileb.niceboat.NiceBoat$Container";
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

    public static class Transformer implements net.minecraft.launchwrapper.IClassTransformer {
        @Override
        public byte[] transform(String name, String transformedName, byte[] basicClass) {
            if (basicClass != null && "net.minecraft.entity.item.EntityBoat".equals(transformedName)) {
                ClassWriter classWriter = new ClassWriter(0);
                new ClassReader(basicClass).accept(new ClassVisitor(ASM_API, classWriter) {
                    @Override
                    public MethodVisitor visitMethod(int access, String name, String descriptor, String signature, String[] exceptions) {
                        if("func_184232_k".equals(name) || "updatePassenger".equals(name)) {
                            return new MethodVisitor(ASM_API , super.visitMethod(access, name, descriptor, signature, exceptions)) {
                                @Override
                                public void visitCode() {
                                    super.visitCode();
                                    this.visitVarInsn(Opcodes.ALOAD, 0);
                                    this.visitVarInsn(Opcodes.ALOAD, 1);
                                    this.visitMethodInsn(Opcodes.INVOKESTATIC, "mods/Hileb/niceboat/NiceBoat$Hook", "onUpdate", "(Lnet/minecraft/entity/item/EntityBoat;Lnet/minecraft/entity/Entity;)V", false);
                                }

                                @Override
                                public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                                    if("func_184454_a".equals(owner) || "applyYawToEntity".equals(owner)) {
                                        visitInsn(Opcodes.POP2);
                                    } else super.visitMethodInsn(opcode, owner, name, descriptor, isInterface);
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

    public static class Container extends DummyModContainer {

        public Container(){
            super(new ModMetadata());
            ModMetadata metadata=this.getMetadata();
            metadata.modId=Tags.MOD_ID;
            metadata.name=Tags.MOD_NAME;
            metadata.description="This mod will make the player's perspective not move with the ship's direction when controlling the ship's movement. At the same time, if the angle between the ship's heading and the player's perspective is less than 22.5°, the ship's heading will gradually be corrected to align with the player's perspective.";
            metadata.version=Tags.VERSION;
            metadata.url="https://github.com/Ecdcaeb/NiceBoat12/";
            metadata.authorList.add("Hileb");
        }

        @Override
        public boolean registerBus(EventBus bus, LoadController controller) {
            return true;
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
