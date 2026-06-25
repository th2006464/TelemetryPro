const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..", "..");
const MAP_DIR = path.join(ROOT, "app", "src", "main", "java", "com", "telemetrypro", "app", "ui", "map");
const DOCS_DIR = path.join(ROOT, "docs");
const WORLD_MAP_DATA = path.join(MAP_DIR, "WorldMapData.kt");
const WORLD_MAP_CITIES = path.join(MAP_DIR, "WorldMapCities.kt");
const LEGACY_GRID = path.join(MAP_DIR, "WorldMapGrid.kt");
const OUTPUT_GRID = LEGACY_GRID;
const OUTPUT_SVG = path.join(DOCS_DIR, "reference_map.svg");

const GRID_W = 360;
const GRID_H = 170;
const MAX_LAT_RAD = 85 * Math.PI / 180;
const MERC_MAX = Math.log(Math.tan(Math.PI / 4 + MAX_LAT_RAD / 2));
const BYTES_PER_ROW = Math.ceil(GRID_W / 8);
const ASIA_FINE_LON_MIN = 40;
const ASIA_FINE_LON_MAX = 160;
const ASIA_FINE_LAT_MIN = -15;
const ASIA_FINE_LAT_MAX = 65;

const EXTRA_POLYGONS = [
  [[61,25],[63,24],[66,24],[68,23],[70,21],[72,19],[73,17],[74,14],[76,11],[77.5,8.5],[79.5,8],[81,8.5],[83,10.5],[85,17],[88,21.5],[90,22.5],[91.5,22.5],[92.5,20],[94,18],[96,15],[98,13],[99.5,10],[100.5,7],[102,5],[103,3],[104,2],[104.5,5],[105,8],[106.5,10.5],[108.5,12.5],[109.5,16],[110,19.5],[111.5,21.5],[113.5,22.5],[115,22.5],[117.5,24],[119.5,27],[121,30],[122,33],[122.5,36],[121,36.5],[119,35.5],[116,33.5],[112,32.5],[108,32.5],[104,30.5],[100,28.5],[96,28],[92,28],[88,27.5],[84,27.5],[80,28],[76,30],[72,32.5],[68,34],[65,33],[63,30],[61,25]],
  [[48,30],[50,29],[52,27],[54,25],[56,24],[58,23],[59,21],[58,19],[56,18],[54,17],[52,16],[50,17],[48,19],[47,22],[47,26],[48,30]],
  [[120,25.5],[121,25.7],[122,25.3],[122.2,24.5],[121.8,23.6],[121.3,22.8],[120.7,22.2],[120,22.3],[120,23.1],[120,24.2],[120,25.5]],
  [[108.6,20.1],[109.5,20.4],[110.4,20.3],[111.1,19.8],[111.2,19],[110.9,18.3],[110.2,18.1],[109.3,18.2],[108.8,18.8],[108.6,19.5],[108.6,20.1]],
  [[105,-5.8],[107,-5.8],[109,-6.2],[111,-6.6],[113.2,-7.4],[114.8,-7.8],[114.7,-8.5],[112.8,-8.6],[110.5,-8.4],[108,-7.8],[106,-7],[105,-5.8]],
  [[119,-1.5],[120.5,0.5],[122,1.5],[123.8,1.2],[124.6,0],[123.8,-1.2],[122.8,-2.5],[121.7,-3.2],[120.5,-4],[119.2,-3.5],[118.8,-2.2],[119,-1.5]],
  [[115,-8],[116.5,-8.1],[118,-8.3],[119.5,-8.5],[121,-8.6],[122.6,-8.7],[123.8,-8.9],[124.8,-9.4],[123.2,-9.8],[121.6,-9.8],[120,-9.6],[118,-9.2],[116.5,-8.8],[115,-8]],
  [[141,45.2],[142.5,46],[144,47],[145,48],[145.5,49],[144.8,50],[143.3,49.5],[142,48.2],[141.2,46.8],[141,45.2]],
  [[126,34],[127.5,34.2],[128.8,34.5],[129.5,35],[129,35.5],[127.8,35.4],[126.6,35],[126,34]],
  [[133,33],[134.4,33.4],[134.8,34.2],[133.8,34.4],[132.8,34],[133,33]],
  [[95,5.8],[96.5,5.4],[97.8,4.7],[99.2,3.6],[100.3,2.4],[101.4,1],[102.3,-0.5],[103,-2],[103.8,-3.8],[104.4,-5.2],[104.8,-5.8],[103.2,-5.8],[101,-4],[99,-2],[97.4,0.5],[96,3.2],[95,5.8]],
  [[-22.8,64.4],[-21,64.5],[-19,64.4],[-18,63.8],[-20,63.5],[-22,63.6],[-22.8,64.4]],
  [[-80,25],[-79.2,25.7],[-78.4,26.2],[-77,26.5],[-76.6,25.4],[-77.8,24.8],[-79,24.6],[-80,25]],
  [[-90.9,14.3],[-90,14.2],[-89.1,14.2],[-89.2,15],[-90.2,15.2],[-90.9,14.3]],
  [[-44.3,-23.5],[-42.4,-23.4],[-42.4,-22.3],[-43.6,-22],[-44.4,-22.5],[-44.3,-23.5]],
  [[-49.4,-16.4],[-47,-16.4],[-47,-15.2],[-49,-15.2],[-49.4,-16.4]],
  [[-79.2,0.5],[-78,0.5],[-77,0.1],[-77,-1.2],[-78.4,-1.4],[-79.2,-0.4],[-79.2,0.5]],
  [[-80.4,-1.6],[-79,-1.6],[-78.7,-2.6],[-79.6,-2.8],[-80.4,-2.2],[-80.4,-1.6]],
  [[-60.6,-3.7],[-59,-3.7],[-59,-2.2],[-60.6,-2.2],[-60.6,-3.7]],
  [[-35.8,-8.6],[-34.4,-8.6],[-34.4,-7.2],[-35.8,-7.2],[-35.8,-8.6]],
  [[-58.8,6],[-57.6,6],[-57.4,7.2],[-58.6,7.5],[-58.8,6]],
  [[174,-37.4],[175.5,-37.4],[176,-36.2],[175.2,-35.3],[174,-35.7],[174,-37.4]],
  [[174.2,-41.8],[175.2,-41.8],[175.4,-40.8],[174.8,-40.5],[174.2,-41],[174.2,-41.8]],
  [[166,-22.8],[167,-22.6],[167.3,-21.9],[166.8,-21.5],[166.1,-21.8],[166,-22.8]],
  [[177.8,-18.4],[178.8,-18.4],[178.9,-17.4],[178,-17.3],[177.8,-18.4]],
  [[-158.6,20.8],[-157,20.8],[-157,22],[-158.6,22]],
  [[35.5,33],[37.2,33],[37.2,34.2],[35.5,34.2],[35.5,33]],
  [[-6.9,53],[-5.8,53],[-5.8,53.7],[-6.9,53.7],[-6.9,53]],
  [[11,47.7],[12.3,47.7],[12.3,48.5],[11,48.5],[11,47.7]],
  [[4.2,45.4],[5.4,45.4],[5.4,46.2],[4.2,46.2],[4.2,45.4]],
  [[-4.2,55.2],[-1.8,55.2],[-1.8,56.5],[-4.2,56.5]],
  [[-16.5,17.7],[-15.3,17.7],[-15.3,18.5],[-16.5,18.5]],
  [[-77.4,-12.4],[-76.6,-12.4],[-76.6,-11.6],[-77.4,-11.6]],
  [[146.7,-9.9],[147.8,-9.9],[147.8,-9.1],[146.7,-9.1]]
];

function parsePolygons(src) {
  const polygons = [];
  const polyRe = /Polygon\(listOf\(([\s\S]*?)\)\)/g;
  let match;
  while ((match = polyRe.exec(src))) {
    const pts = [...match[1].matchAll(/(-?\d+(?:\.\d+)?)f\s+to\s+(-?\d+(?:\.\d+)?)f/g)]
      .map(v => [Number(v[1]), Number(v[2])]);
    if (pts.length >= 3) polygons.push(pts);
  }
  return polygons;
}

function parseCities(src) {
  return [...src.matchAll(/City\("([^"]+)",\s*([-0-9.]+)f,\s*([-0-9.]+)f\)/g)]
    .map(m => ({ name: m[1], lon: Number(m[2]), lat: Number(m[3]) }));
}

function parseLegacyBytes(src) {
  return [...src.matchAll(/(\d+)\.toByte\(\)/g)].map(m => Number(m[1]));
}

function parseBaselineGrid(src) {
  const widthMatch = src.match(/const val GRID_W = (\d+)/);
  const heightMatch = src.match(/const val GRID_H = (\d+)/);
  const rowMatch = src.match(/BYTES_PER_ROW = (\d+)/);
  const width = widthMatch ? Number(widthMatch[1]) : GRID_W;
  const height = heightMatch ? Number(heightMatch[1]) : GRID_H;
  const bytesPerRow = rowMatch ? Number(rowMatch[1]) : Math.ceil(width / 8);
  return {
    width,
    height,
    bytesPerRow,
    bytes: parseLegacyBytes(src)
  };
}

function mercatorY(latDeg, height = GRID_H) {
  const latRad = Math.max(-MAX_LAT_RAD, Math.min(MAX_LAT_RAD, latDeg * Math.PI / 180));
  const yMerc = Math.log(Math.tan(Math.PI / 4 + latRad / 2));
  return height * (1 - yMerc / MERC_MAX) / 2;
}

function inverseMercatorLat(gridY, height = GRID_H) {
  const yMerc = MERC_MAX * (1 - 2 * gridY / height);
  return (2 * Math.atan(Math.exp(yMerc)) - Math.PI / 2) * 180 / Math.PI;
}

function pointInPolygon(lon, lat, polygon) {
  let inside = false;
  for (let i = 0, j = polygon.length - 1; i < polygon.length; j = i++) {
    const [xi, yi] = polygon[i];
    const [xj, yj] = polygon[j];
    const intersect = ((yi > lat) !== (yj > lat)) &&
      (lon < ((xj - xi) * (lat - yi) / ((yj - yi) || 1e-12) + xi));
    if (intersect) inside = !inside;
  }
  return inside;
}

function annotatePolygons(polygons) {
  return polygons.map(pts => {
    let minLon = Infinity;
    let maxLon = -Infinity;
    let minLat = Infinity;
    let maxLat = -Infinity;
    for (const [lon, lat] of pts) {
      if (lon < minLon) minLon = lon;
      if (lon > maxLon) maxLon = lon;
      if (lat < minLat) minLat = lat;
      if (lat > maxLat) maxLat = lat;
    }
    return { pts, minLon, maxLon, minLat, maxLat };
  });
}

function isBaselineLand(baseline, x, y) {
  if (x < 0 || x >= baseline.width || y < 0 || y >= baseline.height) return false;
  const byteIndex = y * baseline.bytesPerRow + Math.floor(x / 8);
  const bitIndex = x % 8;
  return ((baseline.bytes[byteIndex] >> bitIndex) & 1) === 1;
}

function shouldRefineWithFinePolygons(lon0, lon1, rowMinLat, rowMaxLat) {
  return lon0 >= ASIA_FINE_LON_MIN &&
    lon1 <= ASIA_FINE_LON_MAX &&
    rowMaxLat >= ASIA_FINE_LAT_MIN &&
    rowMinLat <= ASIA_FINE_LAT_MAX;
}

function buildGrid(polygons, baseline) {
  const polyData = annotatePolygons(polygons);
  const grid = Array.from({ length: GRID_H }, () => new Uint8Array(GRID_W));

  for (let y = 0; y < GRID_H; y++) {
      const latTop = inverseMercatorLat(y);
      const latBottom = inverseMercatorLat(y + 1);
      const rowMinLat = Math.min(latTop, latBottom);
      const rowMaxLat = Math.max(latTop, latBottom);

      for (let x = 0; x < GRID_W; x++) {
        const lon0 = x / GRID_W * 360 - 180;
        const lon1 = (x + 1) / GRID_W * 360 - 180;
      const baselineX = Math.floor(x / GRID_W * baseline.width);
      const baselineY = Math.floor(y / GRID_H * baseline.height);
      let land = isBaselineLand(baseline, baselineX, baselineY);

      const candidates = polyData.filter(p =>
        !(p.maxLon < lon0 || p.minLon > lon1 || p.maxLat < rowMinLat || p.minLat > rowMaxLat)
      );

      if (!land || shouldRefineWithFinePolygons(lon0, lon1, rowMinLat, rowMaxLat)) {
        let insideSamples = 0;
        for (let sy = 0; sy < 4; sy++) {
          const lat = inverseMercatorLat(y + (sy + 0.5) / 4);
          for (let sx = 0; sx < 4; sx++) {
            const lon = (x + (sx + 0.5) / 4) / GRID_W * 360 - 180;
            for (const poly of candidates) {
              if (pointInPolygon(lon, lat, poly.pts)) {
                insideSamples++;
                break;
              }
            }
          }
        }
        if (insideSamples >= 4) land = true;
      }

      grid[y][x] = land ? 1 : 0;
    }
  }

  return grid;
}

function packGrid(grid) {
  const bytes = [];
  for (let y = 0; y < GRID_H; y++) {
    for (let byteCol = 0; byteCol < BYTES_PER_ROW; byteCol++) {
      let value = 0;
      for (let bit = 0; bit < 8; bit++) {
        const x = byteCol * 8 + bit;
        if (x < GRID_W && grid[y][x]) value |= (1 << bit);
      }
      bytes.push(value);
    }
  }
  return bytes;
}

function gridKotlin(bytes) {
  return [
    "package com.telemetrypro.app.ui.map",
    "",
    "/**",
    " * Generated by tools/map/generate_world_map.js.",
    " * Do not edit by hand; regenerate from polygon sources instead.",
    " */",
    "object WorldMapGrid {",
    "    const val GRID_W = 360",
    "    const val GRID_H = 170",
    "    private const val BYTES_PER_ROW = 45",
    "",
    "    private val bitmap: ByteArray = byteArrayOf(",
    bytes.map(v => "        " + v + ".toByte()").join(",\n"),
    "    )",
    "",
    "    fun isLand(x: Int, y: Int): Boolean {",
    "        if (x < 0 || x >= GRID_W || y < 0 || y >= GRID_H) return false",
    "        val byteIndex = y * BYTES_PER_ROW + (x / 8)",
    "        val bitIndex = x % 8",
    "        return ((bitmap[byteIndex].toInt() shr bitIndex) and 1) == 1",
    "    }",
    "}"
  ].join("\n") + "\n";
}

function gridSvg(grid) {
  const circles = [];
  for (let y = 0; y < GRID_H; y++) {
    for (let x = 0; x < GRID_W; x++) {
      if (grid[y][x]) {
        circles.push(`  <circle cx="${x}" cy="${y}" r="0.22" fill="#d0cec6" />`);
      }
    }
  }
  return [
    `<svg viewBox="0 0 ${GRID_W} ${GRID_H}" xmlns="http://www.w3.org/2000/svg" style="background-color: transparent">`,
    ...circles,
    "</svg>"
  ].join("\n") + "\n";
}

function cityFailures(grid, cities) {
  function isNearLand(gx, gy) {
    for (let dy = -1; dy <= 1; dy++) {
      for (let dx = -1; dx <= 1; dx++) {
        const x = gx + dx;
        const y = gy + dy;
        if (x >= 0 && x < GRID_W && y >= 0 && y < GRID_H && grid[y][x]) return true;
      }
    }
    return false;
  }

  return cities
    .map(city => ({
      ...city,
      gx: Math.max(0, Math.min(GRID_W - 1, Math.floor((city.lon + 180) / 360 * GRID_W))),
      gy: Math.max(0, Math.min(GRID_H - 1, Math.floor(mercatorY(city.lat))))
    }))
    .filter(city => !isNearLand(city.gx, city.gy));
}

function main() {
  const polygonText = fs.readFileSync(WORLD_MAP_DATA, "utf8");
  const cityText = fs.readFileSync(WORLD_MAP_CITIES, "utf8");
  const legacyText = fs.readFileSync(LEGACY_GRID, "utf8");

  const polygons = parsePolygons(polygonText).concat(EXTRA_POLYGONS);
  const cities = parseCities(cityText);
  const baseline = parseBaselineGrid(legacyText);
  const grid = buildGrid(polygons, baseline);
  const packed = packGrid(grid);
  const failures = cityFailures(grid, cities);

  if (failures.length > 0) {
    throw new Error("City validation failed: " + JSON.stringify(failures, null, 2));
  }

  fs.writeFileSync(OUTPUT_GRID, gridKotlin(packed), "utf8");
  fs.writeFileSync(OUTPUT_SVG, gridSvg(grid), "utf8");
  console.log(`Generated ${OUTPUT_GRID} and ${OUTPUT_SVG} with ${cities.length} validated cities.`);
}

main();
