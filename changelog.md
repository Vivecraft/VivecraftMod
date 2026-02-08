- change waist freemove to use body yaw without fbt
- fix shaders with voxy fixes
- fix crash with epicfight, and fix freemove with it
- fix roomscale hitting not working, when camera entity is reset
- fix jump boots binding not taking priority over other bindings, fixes jump climbing with the default bindings
- improve roomscale shield consistency
- allow the backpack actions to be rebindable
- block backpack from activating when attacking or using an interaction
- fix roomscale sneaking not working on ladders/climbables
- add setting to disable vanilla climb mechanic
- add setting to require keybind to climb, instead of auto grabbing
- add setting to allow breaking ladders with roomscale when sneaking
- add audio and visual indicator for nullvr haptics, and a setting to toggle it
- fix pointer keyboard not snapping to the right position with gui Appear Over Block setting
- fix climbey/teleport food exhaustion by moving the logic to the server. added server settings for those
- fix climbey climb jump not working, when jumping towards the wall
- fix camera issue with MCA
- fix crash with AutoTools
- fix roomscale shield blocking not activating cooldown
- add visual indicator when shield is on cooldown (slight tilt forward of the shield)

1.21.9+:
- fix ghost damage effect when using the roomscale shield

1.21.5+:
- fix roomscale shield blocking a second attack, even when not blocking anymore

up to 1.21.4:
- fix black camera and black crosshair on linux
- fix issue with some mods messing up the viewport when they do a blit mid frame