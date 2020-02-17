Shortest route avoiding uphill slopes
-------------------------------------

Based on this https://www.reddit.com/r/Showerthoughts/comments/f4zr9d/in_theory_a_path_could_exist_between_mount/ shower thought, stating

> In theory a path could exist between Mount Everest and the Netherlands where you'd always be walking downhill. A marble could therefore potentially roll for around 6800km.

Unfortunately the premise is wrong, but I was wondering what would be the route with the least amount of climb (going downhill is fine), and what would be the alternative routes if we'd block large slopes as well uphill (again downhill any slope is accepted).

The solution is using NASA's Earth Heightmap as a base, and uses a simple A* style algorithm where the heuristic tries to get as close to the end goal as possible while discouraging going uphill.

Three generated images and routes are available in the `images` directory for three different heuristics, they are also available at google maps at https://drive.google.com/open?id=1NA6uWC96Vovlv1o-uHTS6G7sdo_sHJ6N&usp=sharing
