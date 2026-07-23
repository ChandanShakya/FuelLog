import { DatabaseSync } from 'node:sqlite';
const db = new DatabaseSync('C:/Users/labgu/.local/share/mimocode/mimocode.db', { open: true, readOnly: true });

// Query 1: user messages from prior session
console.log('=== USER MESSAGES (prior session) ===');
const userStmt = db.prepare(`SELECT m.id, json_extract(m.data, '$.role') as role, substr(json_extract(p.data, '$.text'), 1, 500) as text_preview FROM message m JOIN part p ON p.message_id = m.id WHERE m.session_id = 'ses_072b709a4ffeIDpRLvSi9lSzjI' AND json_extract(m.data, '$.role') = 'user' ORDER BY m.time_created, p.time_created;`);
for (const row of userStmt.all()) {
  console.log(JSON.stringify(row));
}

// Query 2: assistant tool calls from prior session
console.log('\n=== ASSISTANT TOOL CALLS (prior session) ===');
const toolStmt = db.prepare(`SELECT m.id, json_extract(p.data, '$.type') as part_type, json_extract(p.data, '$.tool') as tool, substr(json_extract(p.data, '$.text'), 1, 300) as text_preview, substr(CAST(json_extract(p.data, '$.state.output') AS TEXT), 1, 500) as output_preview FROM message m JOIN part p ON p.message_id = m.id WHERE m.session_id = 'ses_072b709a4ffeIDpRLvSi9lSzjI' AND json_extract(m.data, '$.role') = 'assistant' ORDER BY m.time_created, p.time_created;`);
for (const row of toolStmt.all()) {
  console.log(JSON.stringify(row));
}

// Query 3: all parts for prior session with larger preview
console.log('\n=== ALL PARTS (prior session, text and tool) ===');
const allStmt = db.prepare(`SELECT m.id as msg_id, json_extract(m.data, '$.role') as role, json_extract(p.data, '$.type') as part_type, json_extract(p.data, '$.tool') as tool, substr(json_extract(p.data, '$.text'), 1, 500) as text, substr(CAST(json_extract(p.data, '$.state') AS TEXT), 1, 800) as state_preview FROM message m JOIN part p ON p.message_id = m.id WHERE m.session_id = 'ses_072b709a4ffeIDpRLvSi9lSzjI' ORDER BY m.time_created, p.time_created;`);
for (const row of allStmt.all()) {
  console.log(JSON.stringify(row));
}

// Query 4: user statements with keywords
console.log('\n=== USER KEYWORD SEARCH ===');
const kwStmt = db.prepare(`SELECT m.id, substr(json_extract(p.data, '$.text'), 1, 500) as text FROM message m JOIN part p ON p.message_id = m.id WHERE m.session_id = 'ses_072b709a4ffeIDpRLvSi9lSzjI' AND json_extract(m.data, '$.role') = 'user' AND (json_extract(p.data, '$.text') LIKE '%always%' OR json_extract(p.data, '$.text') LIKE '%never%' OR json_extract(p.data, '$.text') LIKE '%remember%' OR json_extract(p.data, '$.text') LIKE '%rule%' OR json_extract(p.data, '$.text') LIKE '%decision%') ORDER BY m.time_created;`);
for (const row of kwStmt.all()) {
  console.log(JSON.stringify(row));
}

db.close();
