import { DatabaseSync } from 'node:sqlite';
const db = new DatabaseSync('C:/Users/labgu/.local/share/mimocode/mimocode.db', { open: true, readOnly: true });

// Count total messages in prior session
const countStmt = db.prepare(`SELECT COUNT(*) as cnt FROM message WHERE session_id = 'ses_072b709a4ffeIDpRLvSi9lSzjI';`);
console.log('Total messages in prior session:', countStmt.get().cnt);

// Get ALL parts with longer output preview for the last 3 messages
const stmt = db.prepare(`SELECT m.id, m.time_created, json_extract(m.data, '$.role') as role, json_extract(p.data, '$.type') as part_type, json_extract(p.data, '$.tool') as tool, substr(json_extract(p.data, '$.text'), 1, 300) as text, substr(CAST(json_extract(p.data, '$.state.output') AS TEXT), 1, 2000) as output FROM message m JOIN part p ON p.message_id = m.id WHERE m.session_id = 'ses_072b709a4ffeIDpRLvSi9lSzjI' ORDER BY m.time_created DESC, p.time_created DESC LIMIT 20;`);
const rows = stmt.all();
for (const row of rows.reverse()) {
  console.log(`\n[${row.role}] time=${row.time_created} type=${row.part_type} tool=${row.tool || 'none'}`);
  if (row.text) console.log(`  text: ${row.text}`);
  if (row.output) console.log(`  output: ${row.output}`);
}

db.close();
