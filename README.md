# Quick Start

* Import build.gradle into IntelliJ idea and run `BitStamp App` configuration
* run `gradle jfxRun` to run application.
* run `gradle jfxJar; gradle jfxNative` to build native executable.

I cannot make gradle to show logs :( To see logs you have to either run app from IDE or
build native app and run it in terminal. Something like this - path/to/bitstamp.app/Contents/MacOS/bitstamp

WARNING: tested only on MacOS.

# Architecture

`ExchangeConnection` interface represent data set with real-time information about one particular symbol like BTC/USD.
Exchange connection provides access to order book and recent trade history. This information can be queried by any
thread at any time. When connection receive new piece of data it updates it internal state and notifies listeners.
Business logic (like trade robots) implemented by attaching listeners to exchange connection and reacting to events.

Each `ExchangeConnection` maintains work queue and use it to update it internal state and call listeners. Internal
state objects such as Order Book maintain two copies of data - one being updated and another available for
everybody to read. Once update completes read-only data swapped with new one and became available to reader. This
is done to avoid locking.

TODO
* Use persistent immutable collections instead of collections provided by JDK to decrease amount of copying.
* Refactor `AbstractConnection` template method into strategy pattern if template methods became too clunky.

## Trade robot

Trade robot listens to trade updates and cound how many ticks happened since last update. If number of tick is
more/less than threshold then it issue buy/sell order and process repeats.

## Frontend

There is basic separation between view and view model. View model listens to exchange and update it internal state.
Thanks to JavaFX these updates automaticaly populate related table views.

