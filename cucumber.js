module.exports = {
  default: {
    require: ['src/test/java/com/macsuite/**/steps/*.java'],
    format: ['progress-bar', 'html:cucumber-report.html'],
    formatOptions: { snippetInterface: 'async-await' }
  }
};
