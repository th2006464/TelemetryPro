const fs = require("fs");
const path = require("path");

const ROOT = path.resolve(__dirname, "..", "..");
const MAP_DIR = path.join(ROOT, "app", "src", "main", "java", "com", "telemetrypro", "app", "ui", "map");
const DOCS_DIR = path.join(ROOT, "docs");
const DATA_DIR = path.join(__dirname, "data");
const OUTPUT_GRID = path.join(MAP_DIR, "WorldMapGrid.kt");
const OUTPUT_SVG = path.join(DOCS_DIR, "reference_map.svg");

// ── Grid config ──
// Asymmetric Mercator: top edge at +85°N, bottom edge at -60°S.
// Antarctica is excluded — it eats vertical space under Mercator and the app
// will never be used there. -60° keeps Cape Horn, Tasmania, NZ South Island.
const GRID_W = 720;
const GRID_H = 360;
const MAX_LAT_NORTH_DEG = 85;
const MAX_LAT_SOUTH_DEG = -60;
const MAX_LAT_NORTH_RAD = MAX_LAT_NORTH_DEG * Math.PI / 180;
const MAX_LAT_SOUTH_RAD = MAX_LAT_SOUTH_DEG * Math.PI / 180;
const MERC_TOP = Math.log(Math.tan(Math.PI / 4 + MAX_LAT_NORTH_RAD / 2));
const MERC_BOTTOM = Math.log(Math.tan(Math.PI / 4 + MAX_LAT_SOUTH_RAD / 2));
const MERC_SPAN = MERC_TOP - MERC_BOTTOM;
const BYTES_PER_ROW = Math.ceil(GRID_W / 8);

// Coarse spatial index (for fast candidate lookup)
const COARSE_COLS = 72;   // 5° per column
const COARSE_ROWS = 36;   // ~4.7° per row

function mercatorY(latDeg) {
    const latRad = Math.max(MAX_LAT_SOUTH_RAD, Math.min(MAX_LAT_NORTH_RAD, latDeg * Math.PI / 180));
    const yMerc = Math.log(Math.tan(Math.PI / 4 + latRad / 2));
    // y=0 at +85°N, y=GRID_H at -60°S
    return GRID_H * (MERC_TOP - yMerc) / MERC_SPAN;
}

function inverseMercatorLat(gridY) {
    const yMerc = MERC_TOP - (gridY / GRID_H) * MERC_SPAN;
    return (2 * Math.atan(Math.exp(yMerc)) - Math.PI / 2) * 180 / Math.PI;
}

// Pre-compute lat for every grid row
const ROW_LATS = Array.from({ length: GRID_H + 1 }, (_, y) => inverseMercatorLat(y));

function gridXToLon(gx) { return gx / GRID_W * 360 - 180; }
function lonToCoarseCol(lon) { return Math.max(0, Math.min(COARSE_COLS - 1, Math.floor((lon + 180) / 360 * COARSE_COLS))); }
function latToCoarseRow(lat) {
    // Bucket by raw lat in [-60, +85]; rows below -60 are not generated.
    const clamped = Math.max(MAX_LAT_SOUTH_DEG, Math.min(MAX_LAT_NORTH_DEG, lat));
    return Math.max(0, Math.min(COARSE_ROWS - 1, Math.floor((clamped - MAX_LAT_SOUTH_DEG) / (MAX_LAT_NORTH_DEG - MAX_LAT_SOUTH_DEG) * COARSE_ROWS)));
}

// ── Point-in-polygon (ray casting, optimized) ──
function pointInRing(lon, lat, ring) {
    let inside = false;
    let j = ring.length - 1;
    for (let i = 0; i < ring.length; i++) {
        const xi = ring[i][0], yi = ring[i][1];
        const xj = ring[j][0], yj = ring[j][1];
        if (((yi > lat) !== (yj > lat)) && (lon < (xj - xi) * (lat - yi) / ((yj - yi) || 1e-12) + xi))
            inside = !inside;
        j = i;
    }
    return inside;
}

function pointInMultiPolygon(lon, lat, geom) {
    const coords = geom.coordinates;
    if (geom.type === "Polygon") {
        if (!pointInRing(lon, lat, coords[0])) return false;
        for (let h = 1; h < coords.length; h++)
            if (pointInRing(lon, lat, coords[h])) return false;
        return true;
    } else if (geom.type === "MultiPolygon") {
        for (const poly of coords) {
            if (!pointInRing(lon, lat, poly[0])) continue;
            let inHole = false;
            for (let h = 1; h < poly.length; h++)
                if (pointInRing(lon, lat, poly[h])) { inHole = true; break; }
            if (!inHole) return true;
        }
    }
    return false;
}

// ── Build coarse spatial index ──
function buildSpatialIndex(features) {
    // Initialize empty buckets
    const buckets = Array.from({ length: COARSE_ROWS }, () =>
        Array.from({ length: COARSE_COLS }, () => [])
    );

    for (const f of features) {
        let minLon = Infinity, maxLon = -Infinity;
        let minLat = Infinity, maxLat = -Infinity;

        const walk = (coords) => {
            for (const c of coords) {
                if (typeof c[0] === "number") {
                    if (c[0] < minLon) minLon = c[0];
                    if (c[0] > maxLon) maxLon = c[0];
                    if (c[1] < minLat) minLat = c[1];
                    if (c[1] > maxLat) maxLat = c[1];
                } else walk(c);
            }
        };
        walk(f.geometry.coordinates);

        // Register this feature in all overlapping coarse cells
        const cMinC = lonToCoarseCol(minLon);
        const cMaxC = lonToCoarseCol(maxLon);
        const cMinR = latToCoarseRow(minLat);
        const cMaxR = latToCoarseRow(maxLat);

        for (let r = cMinR; r <= cMaxR; r++)
            for (let c = cMinC; c <= cMaxC; c++)
                buckets[r][c].push(f.geometry);
    }

    console.log(`  Spatial index: ${COARSE_ROWS}×${COARSE_COLS} buckets`);
    let totalEntries = 0;
    for (let r = 0; r < COARSE_ROWS; r++)
        for (let c = 0; c < COARSE_COLS; c++)
            totalEntries += buckets[r][c].length;
    console.log(`  Total index entries: ${totalEntries}`);
    return buckets;
}

// ── Load data ──
function loadData() {
    const raw = JSON.parse(fs.readFileSync(path.join(DATA_DIR, "land.json"), "utf8"));
    console.log(`Loaded ${raw.features.length} land features from Natural Earth 50m`);
    return raw.features;
}

// ── Build grid ──
function buildGrid(features, spatialIndex) {
    const grid = Array.from({ length: GRID_H }, () => new Uint8Array(GRID_W));
    let landCells = 0;

    for (let y = 0; y < GRID_H; y++) {
        const rowMinLat = ROW_LATS[y + 1];  // note: inverseMercator gives larger lat at lower y
        const rowMaxLat = ROW_LATS[y];
        const cr = latToCoarseRow((rowMinLat + rowMaxLat) / 2);

        for (let x = 0; x < GRID_W; x++) {
            const lonCenter = (x + 0.5) / GRID_W * 360 - 180;
            const cc = lonToCoarseCol(lonCenter);

            // Only check candidates in this coarse bucket
            const candidates = spatialIndex[cr][cc];
            if (candidates.length === 0) continue;

            const latCenter = (ROW_LATS[y] + ROW_LATS[y + 1]) / 2;

            for (const geom of candidates) {
                if (pointInMultiPolygon(lonCenter, latCenter, geom)) {
                    grid[y][x] = 1;
                    landCells++;
                    break;
                }
            }
        }

        if ((y + 1) % 60 === 0 || y === GRID_H - 1)
            process.stdout.write(`\r  Row ${y + 1}/${GRID_H} (${Math.round((y+1)/GRID_H*100)}%)`);
    }

    console.log(`\nLand cells: ${landCells} / ${GRID_W * GRID_H} (${(landCells/(GRID_W*GRID_H)*100).toFixed(1)}%)`);
    return grid;
}

// ── Pack bits into bytes ──
function packGrid(grid) {
    const bytes = [];
    for (let y = 0; y < GRID_H; y++)
        for (let bc = 0; bc < BYTES_PER_ROW; bc++) {
            let val = 0;
            for (let bit = 0; bit < 8; bit++) {
                const x = bc * 8 + bit;
                if (x < GRID_W && grid[y][x]) val |= (1 << bit);
            }
            bytes.push(val);
        }
    return bytes;
}

// ── Output Kotlin file ──
// Encode bitmap as Base64 string to avoid JVM <clinit> method size limit
// (a single byteArrayOf(...) with >~8000 entries exceeds the 64KB method cap).
function generateKotlin(bytes) {
    // Base64-encode the raw byte array
    const buf = Buffer.from(bytes);
    const b64 = buf.toString("base64");
    // Chunk into 200-char lines for readable source
    const lines = [];
    for (let i = 0; i < b64.length; i += 200) {
        lines.push('        "' + b64.slice(i, i + 200) + '"');
    }
    return [
        "package com.telemetrypro.app.ui.map",
        "",
        "/**",
        " * Generated by tools/map/generate_world_map_v2.js",
        " * Grid: " + GRID_W + " x " + GRID_H + " (0.5 deg/cell), source: Natural Earth 50m land.",
        " * Coverage: +85N to -60S (Antarctica excluded). Do not edit by hand.",
        " */",
        "object WorldMapGrid {",
        "    const val GRID_W = " + GRID_W,
        "    const val GRID_H = " + GRID_H,
        "    private const val BYTES_PER_ROW = " + BYTES_PER_ROW,
        "",
        "    private val encoded: String = (",
        lines.join(" +\n"),
        "        )",
        "",
        "    private val bitmap: ByteArray by lazy {",
        "        android.util.Base64.decode(encoded, android.util.Base64.DEFAULT)",
        "    }",
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

// ── Output SVG ──
function generateSvg(grid) {
    const circles = [];
    for (let y = 0; y < GRID_H; y++)
        for (let x = 0; x < GRID_W; x++)
            if (grid[y][x]) circles.push(`  <circle cx="${x}" cy="${y}" r="0.35" fill="#d0cec6" />`);

    return [
        `<svg viewBox="0 0 ${GRID_W} ${GRID_H}" xmlns="http://www.w3.org/2000/svg" style="background-color:#1a1a2e">`,
        ...circles,
        "</svg>"
    ].join("\n") + "\n";
}

// ── Validate cities ──
function validateCities(grid, cityFile) {
    const text = fs.readFileSync(cityFile, "utf8");
    const cities = [...text.matchAll(/City\("([^"]+)",\s*([-0-9.]+)f,\s*([-0-9.]+)f\)/g)]
        .map(m => ({ name: m[1], lon: Number(m[2]), lat: Number(m[3]) }));

    function isNearLand(gx, gy) {
        for (let dy = -2; dy <= 2; dy++)
            for (let dx = -2; dx <= 2; dx++) {
                const x = gx + dx, y = gy + dy;
                if (x >= 0 && x < GRID_W && y >= 0 && y < GRID_H && grid[y][x]) return true;
            }
        return false;
    }

    const failures = [];
    for (const c of cities) {
        const gx = Math.floor((c.lon + 180) / 360 * GRID_W);
        const gy = Math.floor(mercatorY(c.lat));
        if (!isNearLand(gx, gy)) failures.push({ ...c, gx, gy });
    }

    if (failures.length > 0) {
        console.warn("WARNING: Cities not near land:");
        failures.forEach(f => console.warn(`  ${f.name} (${f.lon}, ${f.lat}) -> grid(${f.gx}, ${f.gy})`));
    } else {
        console.log(`All ${cities.length} cities validated OK.`);
    }
    return failures;
}

// ── Main ──
function main() {
    console.log(`=== TelemetryPro Map Generator v2 ===`);
    console.log(`Grid: ${GRID_W} x ${GRID_H} (0.5deg/cell), Source: Natural Earth 50m land\n`);

    const t0 = Date.now();
    const features = loadData();
    const spatialIndex = buildSpatialIndex(features);
    const grid = buildGrid(features, spatialIndex);
    const packed = packGrid(grid);

    fs.writeFileSync(OUTPUT_GRID, generateKotlin(packed), "utf8");
    fs.writeFileSync(OUTPUT_SVG, generateSvg(grid), "utf8");

    validateCities(grid, path.join(MAP_DIR, "WorldMapCities.kt"));

    const elapsed = ((Date.now() - t0) / 1000).toFixed(1);
    console.log(`\nDone! Output files:`);
    console.log(`  ${OUTPUT_GRID}`);
    console.log(`  ${OUTPUT_SVG}`);
    console.log(`Elapsed: ${elapsed}s`);
}

main();
