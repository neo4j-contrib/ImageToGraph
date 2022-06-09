# ImageToGraph tool - convert any image to a graph

## Requirements

Build: maven

Run: java17

## Usage

1. Open a terminal in the root directory of this repo (here).
2. Run `./mvnw package` OR `mvnw.cmd package` on Windows.
3. Run `./run.sh /path/to/img` OR `.\img2graph-bundle\target\jlink\default\bin\img2graph \path\to\image` on Windows.

Alternatively you can start a local web server with `java -jar img2graph-web/target/quarkus-app/quarkus-run.jar` and 
access a UI at http://localhost:8080.

## Arguments

|Argument|Description|
|---|---|
|\<input>|Path to image to operate on|
|--color-depth=\<colorDepth>|Color depth for simplified image. (default: 4)|
|--keep-bg|Keep the background|
|--node-max-radius=\<nodeMaxRad>|Node maximum radius. (default: 10)|
|--node-min-radius=\<nodeMinRad>|Node minimum radius. (default: 3)|
|--node-padding=\<nodePadding>|Node padding. (default: 2)|
|--output=\<output>|Path to output directory. Default to current working directory|
|--rel-max-distance=\<relMaxDist>|Relationship maximum distance. (default: 30)|
|--rels-per-node=\<relsPerNode>|Avg relationships per node. (default: 2)|
|--simplified-colors|Use simplified colors. (default: false)|
|--target-res=\<targetResolution>|Target resolution. Changes size of graph. (default:1024)|
|--transparent-bg|Transparent background for SVG output. (default:false)|
|--open|Opens the generated Graph in Arrows.app. (default:false)|
