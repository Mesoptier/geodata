const fs = require('fs');

function preprocessData(filename) {
    const data = fs.readFileSync(filename, 'utf8');
    const lines = data.split('\t\n').map((line) => {
        return line.split('\t').map((value) => {
            return parseInt(value, 10);
        });
    });
    lines.length--;

    const width = lines.length;
    const height = lines[0].length;

    const points = [];

    for (let x = 0; x < width; x++) {
        for (let y = 0; y < height; y++) {
            const c = lines[x][y];
            if (c !== 0) {
                points.push([x, y, c]);
            }
        }
    }

    const object = {
        width,
        height,
        points,
    };

    fs.writeFileSync(filename.replace('.txt', '.json'), JSON.stringify(object));
}

preprocessData(__dirname + '/data/zuideinde.txt');