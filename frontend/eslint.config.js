import js from '@eslint/js'
import eslintPluginVue from 'eslint-plugin-vue'
import ts from 'typescript-eslint'
import globals
  from 'globals'

export default ts.config(
  {
    ignores: ['node_modules/', 'dist/', '**/*.js']
  },
  js.configs.recommended,
  ...ts.configs.recommended,
  ...eslintPluginVue.configs['flat/essential'],
  {
    files: ['*.vue', '**/*.vue'],
    languageOptions: {
      globals: {
        ...globals.browser
      },
      parserOptions: {
        parser: '@typescript-eslint/parser'
      }
    }
  }
)
