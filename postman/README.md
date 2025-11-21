Instrucciones para ejecutar la colección Postman con Newman

Requisitos:
- Node.js + npm instalados
- Newman y el reporter HTML: `npm install -g newman newman-reporter-html`

Pasos:
1. Arranca la API localmente: `./mvnw spring-boot:run` (o asegúrate que esté corriendo en `http://localhost:8080`).
2. Ajusta `postman/logitrack_environment.json` si tu API corre en otro puerto.
3. Ejecuta el script PowerShell:

   ```powershell
   cd postman
   .\run_newman.ps1
   ```

4. Revisa los reportes en `postman/results/<timestamp>/report.html` y `report.json`.

Notas:
- El request `Auth - Login (sets auth_token)` está configurado para extraer el `token` de la respuesta y guardarlo en la variable de entorno `auth_token`.
- Si necesitas credenciales específicas, modifica el body del request `Auth - Login` en la colección o actualiza el environment con `auth_token` manualmente.
