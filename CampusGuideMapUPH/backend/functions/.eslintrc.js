module.exports = {
  root: true,
  env: { node: true, es2021: true },
  parser: '@typescript-eslint/parser',
  parserOptions: {
    project: ['./tsconfig.json'],
    tsconfigRootDir: __dirname,
    sourceType: 'module',
  },
  plugins: ['@typescript-eslint'],
  extends: [
    'eslint:recommended',
    'plugin:@typescript-eslint/recommended',
  ],
  ignorePatterns: ['lib/**/*', 'node_modules/**/*', '.eslintrc.js'],
  rules: {
    '@typescript-eslint/no-explicit-any': 'off',
    'object-curly-spacing': 'off',
    'no-trailing-spaces': 'off',
    'require-jsdoc': 'off',
  },
};
