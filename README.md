# 20-06-2025

## Pre-ALPHA Version Changes

- Added "Maintenance" system
- Added Web Server API **[BETA]**

###  **What is "Maintenance" system?**
> Maintenance system is, since ODS Core is an open source plugin, it is a system created to back up the specified files every time the server is started in order to prevent damage to config and similar files during the development phase and to prevent players who are not on the whitelist from entering the server.

### **How does the maintenance system work?**
> It has a system similar to the whitelist system found in paper/spigot software [for now, GUI style additions may be made in _future updates_] The usage method is very simple, you can add or remove players to the list, view the list, and clear the list by typing

### **What is Web Server API?**
>I am thinking of developing it as a panel where you can perform many controls such as server controls, settings, player controls. As a result,
you will be able to control the settings that are difficult or cannot be done via the command prompt via this panel.<br><br>
Also, since it is a panel that is only available on localhost, it is almost impossible to access from outside 
(an external connection cannot be made without access to the device), apart from that, even if there is a connection to the device, 
it is a difficult system to break because it has its own login system.



# <u>**Command & Permissions**</u>
| COMMANDS                  | USAGES                                         |
|---------------------------|------------------------------------------------|
| /wl add username          | adds user to white-list                        | 
| /wl remove username       | removes user from white-list                   |
| /wl list                  | shows the white-list list                      |
| /wl clear                 | deletes everyone from the list                 |
| /ods maintenance on / off | activates / deactivates the maintenance system |