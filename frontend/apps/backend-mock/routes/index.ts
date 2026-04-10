import {defineEventHandler} from 'h3';

export default defineEventHandler(() => {
  return `
<h1>EvolveHub Mock Service</h1>
<h2>Available APIs:</h2>
<ul>
<li><a href="/api/user/list">/api/user/list</a></li>
<li><a href="/api/role/list">/api/role/list</a></li>
<li><a href="/api/dept/list">/api/dept/list</a></li>
<li><a href="/api/permission/list">/api/permission/list</a></li>
</ul>
`;
});
