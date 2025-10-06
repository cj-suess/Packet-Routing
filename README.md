# Packet Routing
## Using Minimum Spanning Trees to Route Packets in a Network Overlay
In this program, a network of nodes can be created to pass information to one another. It does so by constructing a logical overlay over a distributed set of nodes, and then computing a minimum spanning tree to route packets in the system. This is accomplished through a series of steps:
1. A Registry is created to manage the network of Messaging nodes
2. n Messaging nodes are spawned with uniquely identifying IP addresses and server ports
3. An overlay is generated creating a k-regular graph which dictates how many connections each Messaging node should have
4. The Registry will create a MST (Minimum Spanning Tree) that the Messaging nodes will use to route messages to each other
5. All marshalling/unmarshalling is performed manually for each Event type
6. All TCP connections are established manually
