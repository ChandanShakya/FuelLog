import { DatabaseSync } from 'node:sqlite';
const db = new DatabaseSync('C:/Users/labgu/.local/share/mimocode/mimocode.db', { open: true, readOnly: true });

// Get the last few assistant messages from the prior session (the build+install part)
const stmt = db.prepare(`SELECT m.id, json_extract(m.data, '$.role') as role, substr(json_extract(p.data, '$.text'), 1, 600) as text, substr(CAST(json_extract(p.data, '$.state.output') AS TEXT), 1, 1500) as output, json_extract(p.data, '$.type') as part_type, json_extract(p.data, '$.tool') as tool FROM message m JOIN part p ON p.message_id = m.id WHERE m.session_id = 'ses_072b709a4ffeIDpRLvSi9lSzjI' ORDER BY m.time_created, p.time_created;`);
const rows = stmt.all();
// Print only the last 15 entries to see the end of the session
const lastRows = rows.slice(-15);
for (const row of lastRows) {
  console.log(`[${row.role}] type=${row.part_type} tool=${row.tool || 'none'}`);
  if (row.text) console.log(`  text: ${row.text}`);
  if (row.output) console.log(`  output: ${row.output.substring(0, 800)}`);
  console.log('');
}

db.close();
