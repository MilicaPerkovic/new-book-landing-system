# Dockerfile for Book Service

# Stage 1: Build
FROM mcr.microsoft.com/dotnet/sdk:8.0-alpine AS builder

WORKDIR /app

# Copy project files
COPY src/ ./

# Restore and build
RUN dotnet restore "BookService.API/BookService.API.csproj"
RUN dotnet build "BookService.API/BookService.API.csproj" -c Release -o /app/build

# Publish
RUN dotnet publish "BookService.API/BookService.API.csproj" -c Release -o /app/publish

# Stage 2: Runtime
FROM mcr.microsoft.com/dotnet/aspnet:8.0-alpine AS runtime

WORKDIR /app

# Install health check dependencies
RUN apk add --no-cache curl

# Copy published app from builder
COPY --from=builder /app/publish .

# Set environment variables
ENV ASPNETCORE_URLS=http://+:5000
ENV ASPNETCORE_ENVIRONMENT=Production

# Health check
HEALTHCHECK --interval=30s --timeout=3s --start-period=5s --retries=3 \
    CMD curl -f http://localhost:5000/health || exit 1

# Expose port
EXPOSE 5000

# Run application
ENTRYPOINT ["dotnet", "BookService.API.dll"]
