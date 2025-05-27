# Traffic Simulation

A configurable traffic intersection simulation with multiple traffic light controller types.

## Overview



This project simulates traffic flow through an intersection controlled by various types of traffic light systems. It offers both visual simulation mode and file-based processing for batch simulations.

## Features

- Multiple traffic light controller types:
  - **Standard**: Traditional cyclic controller that rotates through directions
  - **Priority**: Traffic-density aware controller that prioritizes directions with more waiting vehicles
  - **Opposing**: Controller that handles opposing traffic flows and specialized turning movements
- Visual simulation mode for real-time observation
- JSON-based input and output for batch processing
- Support for various vehicle movements (straight, left turn, right turn)

## Usage

The application can be run in two modes:

### Visualization Mode

```
java -jar simulation.jar vis
```

Launches the graphical simulation visualizer where you can observe traffic flow in real-time.

### File Processing Mode

```
java -jar simulation.jar <inputFile.json> <outputFile.json> [controllerType]
```

Process a simulation based on JSON input and generate results to an output file.

#### Parameters:

- `inputFile.json`: JSON file containing simulation commands
- `outputFile.json`: Path where the simulation results will be saved
- `controllerType` (optional): Type of traffic light controller to use
  - Valid options: `standard`, `priority`, `opposing`
  - Default: `standard`

### Examples:

```
java -jar simulation.jar vis
java -jar simulation.jar input.json output.json
java -jar simulation.jar input.json output.json opposing
```

## Input File Format

The input file should be a JSON file with the following structure:

```json
{
  "commands": [
    {
      "type": "addVehicle",
      "vehicleId": "car1",
      "startRoad": "north",
      "endRoad": "south"
    },
    { "type": "step" },
    {
      "type": "addVehicle",
      "vehicleId": "car2",
      "startRoad": "east",
      "endRoad": "west"
    },
    { "type": "step" }
  ]
}
```

Supported commands:
- `addVehicle`: Adds a vehicle to the simulation
- `step`: Advances the simulation by one step

Valid directions: `north`, `east`, `south`, `west`

## Output File Format

The output file will be a JSON file with the following structure:

```json
{
  "controllerType": "opposing",
  "stepStatuses": [
    { "leftVehicles": ["car1"] },
    { "leftVehicles": [] },
    { "leftVehicles": ["car2"] }
  ]
}
```

The `leftVehicles` array contains the IDs of vehicles that have successfully crossed the intersection during that step.

## Controller Types

### Standard Controller

The standard controller cycles through directions in a fixed clockwise pattern (north → east → south → west → north) with proper light transitions (green → yellow → red → red-yellow → green).

### Priority Controller

The priority controller gives precedence to directions with more waiting vehicles, adapting to traffic density.

### Opposing Controller

The opposing controller handles opposing traffic flows and has special rules for different turning movements:
- Left turns are only allowed on yellow
- Straight and right turns are allowed on green