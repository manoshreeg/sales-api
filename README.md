# Sales Analytics API (Spring Boot)
Run: `mvn spring-boot:run` (JDK 17+). Swagger UI: http://localhost:8080/swagger-ui.html
Seeded admin: admin / admin123 (change it). New registrations get the ANALYST role.

```
curl -X POST localhost:8080/api/auth/login -H "Content-Type: application/json" -d '{"username":"admin","password":"admin123"}'
curl -X POST localhost:8080/api/sales/upload -H "Authorization: Bearer TOKEN" -F file=@sample-sales.csv
curl localhost:8080/api/sales/reports/by-region -H "Authorization: Bearer TOKEN"
curl "localhost:8080/api/sales?region=north&page=0&size=5&sort=saleDate,desc" -H "Authorization: Bearer TOKEN"
```
Endpoints: POST /api/auth/register, POST /api/auth/login, POST /api/sales/upload (ADMIN),
GET /api/sales, GET /api/sales/reports/by-region | by-month | top-products?limit=5

## Rule-based product categorization
Each uploaded product name is matched against keyword rules (lowest priority number wins, then lowest id).
No match -> `Uncategorized` (goes to the review queue). Default rules are seeded on first run.
Example: "Shaving Razor Pro" matches `razor` (priority 10) before `shaving` (priority 30) -> Razors.

```
GET  /api/rules                              list rules
POST /api/rules                              (ADMIN) {"keyword":"trimmer","category":"Hair Removal","priority":10}
DELETE /api/rules/{id}                       (ADMIN)
GET  /api/rules/test?product=Wax%20Strips    dry run: shows the rule that fires
POST /api/sales/recategorize                 (ADMIN) re-run rules on existing sales
GET  /api/sales/uncategorized                review queue
GET  /api/sales/reports/by-category          revenue per category
GET  /api/sales/reports/category-coverage    % of sales categorized
```
Expected result for sample-sales.csv: 6 saved, 2 rejected (bad quantity, blank product), 1 uncategorized (Vitamin Water).
