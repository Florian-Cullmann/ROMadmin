FROM node:22-alpine AS deps
RUN corepack enable && corepack prepare pnpm@9 --activate
WORKDIR /app
COPY package.json pnpm-workspace.yaml pnpm-lock.yaml ./
COPY packages/shared/package.json packages/shared/
COPY packages/backend/package.json packages/backend/
COPY packages/frontend/package.json packages/frontend/
RUN pnpm install --frozen-lockfile

FROM deps AS build
COPY . .
RUN pnpm --filter @romadmin/shared build
RUN pnpm --filter @romadmin/frontend build
RUN pnpm --filter @romadmin/backend build

FROM node:22-alpine AS production
RUN corepack enable && corepack prepare pnpm@9 --activate
WORKDIR /app
COPY --from=build /app/package.json /app/pnpm-workspace.yaml /app/pnpm-lock.yaml ./
COPY --from=build /app/packages/shared/package.json packages/shared/
COPY --from=build /app/packages/shared/dist packages/shared/dist/
COPY --from=build /app/packages/backend/package.json packages/backend/
COPY --from=build /app/packages/backend/dist packages/backend/dist/
COPY --from=build /app/packages/backend/prisma packages/backend/prisma/
COPY --from=build /app/packages/frontend/dist packages/frontend/dist/
RUN pnpm install --prod --frozen-lockfile
RUN pnpm --filter @romadmin/backend exec prisma generate
EXPOSE 3000
CMD ["node", "packages/backend/dist/server.js"]

FROM deps AS development
COPY . .
EXPOSE 3000 5173
CMD ["pnpm", "dev"]
