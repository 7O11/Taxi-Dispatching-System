# Taxi-dispatching-System

## input

* File input: "Load filename", filename is the path of input file.

* Passenger request input: "[CR,(srcx,srcy),(dstx,dsty)]"，0<=srcx,srcy,dstx,dsty<=79.

* Road switcher: "(x1,y1),(x2,y2),status"，status：0 means close, 1 means open.

* Query specific taxi informatioin: "GetStatusTaxis Status", Status: WAITING, PICKING, STOPING, SERVING.

* Query taxies with specific status: "GetSpecialTaxiServeInfo num", 0<=num<=29.

* Query super taxies' serve information: "GetSpecialTaxiServeInfo num", 0<=num<=29.

  *(Terminal Commands)*

## output

The results of each command, and error information.

## other instructions

1. User need to ensure the input file exist and its content is correct, and do not have surplus \r, \n etc. in your file, or errors may occure while reading your input file.
2. Only the road connet two adjacent nodes can be open or close, or you will see some warning information.
3. Car flow: flow count in the past 500ms.
4. A Taxi need to spend 500ms to pass one road, and query window size is 7500ms.
5. Your input file only can be used for init. 
6. Every taxi have four different status: "SERVING","PICKING","WAITING","STOPING".
7. If a taxi's status is set to "SERVING" at the beginning, the path of this taxi is the one correspond to lowest flow to (1,0).
8. If a taxi's status is set to "PICKING" at the beginning, the path of this taxi is from (0,0) to (1,0).
9. If a taxi's status is set to "STOPING" at  the beginning, the taxi will stop for 1 second then convert to "WAITING".
10. Every taxi will determine the lowest flow path and follow it. However, they will check the status of road which is front of them everytime, and if this road is not available, they will recalculate their optimal path.
11. Format in input file should follow the requirements below:
    * '#No 9 Test File#' in the first line.
    * Then, '#map' in the followed line.
    * '#end_map' is need and correspond to '#map'.
    * content of map can be path of a file.
    * '#light' and '#end_light' have one line each.
    * '#taxi' and '#end_taxi' have one line each.
    * '#request' and '#end_request' have one line each.
    * light information can store in a file.
    * Examples in folder called 'test_files_examples'.
12. When you input 'DONE' in termianl, the system will stop but this program is not truly over.
13. Requests with time difference less than 100ms will be regarded as the same request.
14. Only accept up to 300 requests within 100ms, and if you input more than 300 request within 100ms, this program will be over.
15. The length of light is random between 500ms to 1000ms, and all lights have same time length, but their direction is random set at the beginning.
16. If user set light located at other place except crossrods, system will warn you and ignore this light.
17. Red path in GUI not include destination, and the exact final serve information of a taxi is outputed in termianl.
18. Please provide at least 800M memory for test.
19. Use "GetSpecialTaxiServeInfo num" command, users can get serve information of special taxies.
20. Special taxi can pass t o those road exist at the beginning but was closed afterwards.
21. Taxies No. 0~29 are special taxi regardless you appoint them.
22. After a road is closed, their flow will be set to 0, but when special taxi pass these road, their flow will increase.

## Liskov Substitution Principle

The SpecialTaxi class inherits the Taxi class, but only records the service information of the car on the original basis and backs up the initial map accordingly, so that the super taxi can walk the road that is closed behind. Replacing any place where Taxi appears with the corresponding SpecialTaxi class will not destroy the behavior of the program, it is just an upgrade of a regular taxi into a super taxi, which is in line with the LSP principle.