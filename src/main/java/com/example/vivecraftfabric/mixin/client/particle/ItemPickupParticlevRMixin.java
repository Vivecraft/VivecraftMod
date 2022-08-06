//package com.example.vivecraftfabric.mixin.client.particle;
//
//import com.example.vivecraftfabric.GameRendererExtension;
//import net.minecraft.client.Minecraft;
//import net.minecraft.client.particle.ItemPickupParticle;
//import net.minecraft.util.Mth;
//import net.minecraft.world.entity.player.Player;
//import net.minecraft.world.phys.Vec3;
//import org.spongepowered.asm.mixin.Final;
//import org.spongepowered.asm.mixin.Mixin;
//import org.spongepowered.asm.mixin.Shadow;
//import org.spongepowered.asm.mixin.injection.At;
//import org.spongepowered.asm.mixin.injection.Redirect;
//
//@Mixin(ItemPickupParticle.class)
//public class ItemPickupParticlevRMixin {
//
//    @Final
//    @Shadow
//    private Player target;
//
//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 0), method = "render")
//    public double updateX(double d, double e, double f) {
//        Vec3 pos = this.target.position().add(0, 0.5f, 0);
//
//        Minecraft mc = Minecraft.getInstance();
//        if (target == mc.player) {
//            pos = ((GameRendererExtension)mc.gameRenderer).getControllerRenderPos(0);
//        }
//
//        return Mth.lerp(d, pos.x, pos.x);
//    }
//
//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 1), method = "render")
//    public double updateY(double d, double e, double f) {
//        Vec3 pos = this.target.position().add(0, 0.5f, 0);
//
//        Minecraft mc = Minecraft.getInstance();
//        if (target == mc.player) {
//            pos = ((GameRendererExtension)mc.gameRenderer).getControllerRenderPos(0);
//        }
//
//        return Mth.lerp(d, pos.y, pos.y);
//    }
//
//    @Redirect(at = @At(value = "INVOKE", target = "Lnet/minecraft/util/Mth;lerp(DDD)D", ordinal = 2), method = "render")
//    public double updateZ(double d, double e, double f) {
//        Vec3 pos = this.target.position().add(0, 0.5f, 0);
//
//        Minecraft mc = Minecraft.getInstance();
//        if (target == mc.player) {
//            pos = ((GameRendererExtension)mc.gameRenderer).getControllerRenderPos(0);
//        }
//
//        return Mth.lerp(d, pos.z, pos.z);
//    }
//}
