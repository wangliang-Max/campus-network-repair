# 前端静态资源在宿主机执行 npm ci && npm run build 生成 dist/。
# 构建阶段整理产物，运行阶段使用 Nginx；后端镜像为 Maven -> JRE 多阶段构建。
FROM nginx:1.27-alpine AS dist
COPY dist /usr/share/nginx/html

FROM nginx:1.27-alpine
ENV BACKEND_HOST=backend
COPY nginx/default.conf.template /etc/nginx/templates/default.conf.template
COPY --from=dist /usr/share/nginx/html /usr/share/nginx/html
EXPOSE 80
