# Forge 1.16.5 validation

Use JDK 17 to launch Gradle and provide a JDK 8 toolchain. The game and compiled mod target Java 8.

```powershell
.\gradlew.bat --no-daemon --max-workers=1 "-Dorg.gradle.jvmargs=-Xmx1G" clean assemble sourcesJar
```

The main JAR in `build/libs/` runs through `reobfJar` for production Forge. Both it and the source archive include the MIT license.

## Native behavior checks

Version 1.0.3 passed 15 native checks with its final packaged JAR on Minecraft 1.16.5, Forge 36.2.39, and Java 8. An isolated dedicated-server harness used real `ServerPlayerEntity` and `MobEntity` instances on the server thread without transforming game or mod classes.

The checks cover:

- The registered login callback grants protection. Native incoming damage and outgoing damage to living entities are blocked while protected; unprotected controls take damage.
- Native `MobEntity.setAttackTarget` accepts an unprotected player and clears a protected player. The resulting null-target event is safe, and a stale event cannot clear a different current target.
- Changing yaw and pitch alone preserves protection. Displacement of 0.05 blocks preserves it; displacement of 0.1001 blocks ends it. Damage and targeting resume afterward.
- A one-second configuration grants protection initially and expires through the normal server tick handler after real elapsed time. Targeting resumes afterward.
- The exact packaged JAR loads as version 1.0.3, and the server saves and stops normally.

To reproduce the native checks, invoke the registered login callback with a native login event, then use the real damage and target methods with protected and unprotected controls. Clear the newly constructed player's vanilla spawn immunity before checking damage. Run movement checks through the normal server tick event and wait for real elapsed time for expiry.

These checks use synthetic server players and call the login callback directly. They do not verify a connected client, network negotiation, the protection icon, or full mob pathfinding. Test those separately with a Forge client and server, using a fresh world.
