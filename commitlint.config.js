// commitlint.config.js — AlphaKids KMP
// Conventional commits with scoped types for the project.

module.exports = {
  extends: ['@commitlint/config-conventional'],
  rules: {
    'type-enum': [
      2,
      'always',
      ['feat', 'fix', 'docs', 'style', 'refactor', 'test', 'chore', 'infra'],
    ],
  },
};
