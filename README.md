# Note

A decade later I have learned quite a lot implementing event sourced systems, and I would implement a few things a bit differently nowadays.  In particular, I would recommend use a separate table for event streams and an `N:M` mapping between events and stream, such that each event can be part of multiple streams.  This is not a must, though, as I have successfully implemented several systems that are in production right now that use a similar table layout as this solution. 

I'm no longer developing this event store and therefore merely keep it as a reference implementation for other people to learn about event sourcing and investigate. 

JEEventStore
============

[![Build Status](https://travis-ci.org/JEEventStore/JEEventStore.png?branch=master)](https://travis-ci.org/JEEventStore/JEEventStore)

## Supported Storage Engines

### Relational Databases
[Complete] JPA 2.0 (any supported database)

[Planned] JPA 2.0 w/ Master (writes) / Slave (reads) support

### Document Databases
[Planned] MongoDB

### Other
[Planned] File based storage

[Planned] In-memory

## Supported Serialization Engines

### Raw Serializers
[Complete] JSON w/ Google Gson

### Hooks
[Planned] Memcached deserialization cache

[Planned] Encryption layer

