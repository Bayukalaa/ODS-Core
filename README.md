# 20-06-2025

## Pre-ALPHA Version Changes

- Added "Maintenance" system


###  **What is "Maintenance" system?**
> Maintenance system is, since ODS Core is an open source plugin, it is a system created to back up the specified files every time the server is started in order to prevent damage to config and similar files during the development phase and to prevent players who are not on the whitelist from entering the server.
### **How does the maintenance system work?**
> It has a system similar to the whitelist system found in paper/spigot software [for now, GUI style additions may be made in _future updates_] The usage method is very simple, you can add or remove players to the list, view the list, and clear the list by typing

| COMMAND             | usage                           |
|---------------------|---------------------------------|
| /wl add username    | adds user to white-list         | 
| /wl remove username | removes user to white-list      |
| /wl list            | shows the white-list list       |
| /wl clear           | deletes everyone from the list  | 