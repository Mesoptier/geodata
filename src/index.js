import * as d3 from 'd3';

const config = {
    gridSize: 10,
    gridPadding: 1,
};

const loadFile = require.context('../data', false, /\.json$/);
const filenames = loadFile.keys();

// Data picker
const select = d3.select('body')
    .append('select')
    .on('change', () => {
        const filename = select.property('value');
        updatePoints(filename);
    });

const options = select
    .selectAll('option')
    .data(filenames).enter()
    .append('option')
    .text(d => d);

const svg = d3.select('body')
    .append('svg');

function updatePoints(filename) {
    const data = loadFile(filename);

    svg
        .attr('width', data.width * config.gridSize)
        .attr('height', data.width * config.gridSize);

    const circle = svg
        .selectAll('circle')
        .data(data.points);

    circle.enter()
        .append('circle')
            .attr('r', d => (config.gridSize / 2) - config.gridPadding)
        .merge(circle)
            .attr('cx', d => d[0] * config.gridSize)
            .attr('cy', d => d[1] * config.gridSize)
            .style('fill', d => ['', '#ff7b9d', '#56c300', '#00b6ff'][d[2]]);

    circle.exit().remove();
}

updatePoints(filenames[0]);
