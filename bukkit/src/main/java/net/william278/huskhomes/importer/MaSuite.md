# Importing from MaSuite

## Preparing for Import

1. Ensure the max homes permissions are set correctly for all users that own the homes you are importing.  To ensure this, change your  `masuitehomes.home.limit.global.<amount>` permissions to `huskhomes.max_homes.<amount>` on all relevant groups or users.  Note you can search by permission in the LuckPerms WebEditor, in the top right next to Save.  In addition to changing these permissions, ensure you have all relevant users saved in the LuckPerms DB, i.e. on your production DB or a copy.

2. Deploy the necessary files to each server.  For deploying, you should use a build of the HuskHomes plugin from my branch, the latest being `HuskHomes-Plugin-4.3.2-MaSuite.jar`.  Additionally, under the `HuskHomes` directory, copy the `config.yml` from your Proxy's (Bungee, Velocity) `plugins/MaSuite` directory, renamed to `masuite.yml`.  This contains the DB credentials for the MaSuite DB.  These two files can then be deployed to each server's `plugin` directory with `deploy.sh`.

3. Delete the release version of the HuskHomes plugin from the servers.  The latest is `HuskHomes-Plugin-4.3.2.jar`, which you can delete from `plugins` with `delete.sh`.

## Performing Import

  *This process will be applied to every server.  Start with any server you like.*

1. If the server is running, stop it.
2. Start the server.
3. From console, run `huskhomes import start MaSuite`.  If this does not work, confirm the preparation steps worked by checking that the plugin file is named `HuskHomes-Plugin-4.3.2-MaSuite.jar`, there is no `HuskHomes-Plugin-4.3.2.jar`, and there is a `masuite.yml` under `HuskHomes`.  Homes that could not be imported will be logged to console.  The most common cause of this is that groups do not have the correct home permission, or the user in question is not in the current LuckPerms DB.
4. Stop the server.
5. Start this section over with a different server until done.

## Cleaning up after Import

1. I'd recommend going back to the release version of HuskHomes to be safe.  To do this, delete `HuskHomes-Plugin-4.3.2-MaSuite.jar` with `delete.sh`, and deploy `HuskHomes-Plugin-4.3.2.jar` with `deploy.sh`.
2. You can also remove `masuite.yml` from `HuskHomes` with `delete.sh` as it's no longer needed.