# frontend

This is an example project for a single page application frontend with Vue.js v3. The UI is accessible at <http://localhost:5173>.

## Prerequisites

Install [Node.js 24.X](https://nodejs.org/en/) or use a Node Version Manager and ensure that the root folder of its installation is added to your PATH. You can check with this command: `echo %PATH%` (or echo $PATH on Linux / Git Bash).

## Development Setup

There are multiple possibilities, here are a few examples:

- [VSCode](https://code.visualstudio.com/) + [Volar](https://marketplace.visualstudio.com/items?itemName=Vue.volar) (and disable Vetur) + [TypeScript Vue Plugin (Volar)](https://marketplace.visualstudio.com/items?itemName=Vue.vscode-typescript-vue-plugin).
- [WebStorm](https://www.jetbrains.com/webstorm/) (brings the required plugins natively)
- your favorite text editor + CLI

## Project Setup

Install the required dependencies with

```sh
npm install
```

Then you are good to go with

```sh
npm run dev
```

Then, the site is available at <http://localhost:5173/>.

To deploy your code to production, you can compile and minify using

```sh
npm run build
```

Also, there is linter available:

```sh
npm run lint
```
