/**
 * Add the searched organizations by keyword
 */
function searchOrgs(page) {
  const pageElement = document.getElementById('current-page');
  // -1 for prev page and -2 for next page
  if (page === -1) {
    if (pageElement.innerText > 0) {
      pageElement.innerText--;
    }
  } else if (page === -2) {
    pageElement.innerText++;
  } else {
    pageElement.innerText = page;
  }
  removeOrgs();
  const keyword = document.getElementById('keyword').value;
  const qs = '/sql?' + updateQueryString('keyword', keyword) + '&' + updateQueryString('page', pageElement.innerText);
  addTitle(keyword);
  addPagination();
  addOrgs(qs);
}

function addPagination() {
  document.getElementById('pagination').style.display = 'inline-block';
}

function addOrgs(qs) {
  fetch(qs).then(response => response.json()).then(text => {
    const orgsContainer = document.getElementById('existing-organizations');
    text.forEach(entry => {
      orgsContainer.appendChild(getOrgAsHtmlDescription(entry));
    });
  });
}
 
function removeOrgs() {
  // remove previously displayed similar organizations
  var existingOrgs = document.getElementById('existing-organizations');
  while (existingOrgs.firstChild) {
    existingOrgs.removeChild(existingOrgs.firstChild);
  }
}
 
/**
 * Returns an updated URL search param
 */
function updateQueryString(key, value) {
  var searchParams = new URLSearchParams();
  searchParams.append(key, value);
  return searchParams;
}
 
/**
 * Creates list element from org
 */
function getOrgAsHtmlDescription(org) {
  const orgElement = document.createElement('li');
  orgElement.setAttribute('id', 'org');

  var newDiv = document.createElement('div'); 
  newDiv.setAttribute('id', 'name-about');

  const nameElement = document.createElement('a');
  nameElement.setAttribute('id', 'org-name');
  nameElement.setAttribute('href', 'https://' + org.link);
  nameElement.setAttribute('target', '_blank');
  nameElement.innerText = org.name;
  newDiv.appendChild(nameElement);

  const aboutElement = document.createElement('p');
  aboutElement.setAttribute('id', 'about');
  aboutElement.innerText = org.about;
  newDiv.appendChild(aboutElement);
  orgElement.appendChild(newDiv);

  const neighborElement = document.createElement('p');
  neighborElement.setAttribute('id', 'like-this');
  neighborElement.innerText = 'Like this: ' + org.neighbor1 + ', ' + org.neighbor2 + ', ' + org.neighbor3 + ', ' + org.neighbor4;
  orgElement.appendChild(neighborElement);

  // make the whole list element clickable and take user to organization.html pasing id as parameter
  orgElement.onclick = function() { redirect(org.id); }
  return orgElement;
}

/**
 * Redirects user to organization.html
 */
function redirect(id) {
  const qs = updateQueryString('id', id);
  const redirect = '/organization.html?' + qs;
  window.location = redirect;
}
 
function addListener() {
  const inputBox = document.getElementById('keyword');
  inputBox.addEventListener('keyup', function(event) {
      if (event.key === 'Enter') {
        searchOrgs(0);
      }
  });
}
 
function addTitle(keyword) {
  const element = document.getElementById('results-title');
  element.innerText = 'Results for [' + keyword + ']: ';
}

function getClassifications() {
  const qs = '/data';
  const classDiv = document.getElementById('roots');
  fetch(qs).then(response => response.json()).then(tree => {
    tree.roots.forEach(root=> {
      classDiv.appendChild(addToClassTree(tree, root, root));
    });
  }); 
}

function addToClassTree(tree, parent, classPath) {
  const parentElem = document.createElement('li');
  parentElem.setAttribute('value', classPath);
  if (!(tree[parent].length === 0)) {
    const icon = document.createElement('a');
    icon.innerText = parent;
    icon.href = '#';
    parentElem.appendChild(icon);
    parentElem.setAttribute('class', 'uk-parent');
    const nested = document.createElement('ul');
    nested.setAttribute('class', 'uk-nav-sub');
    tree[parent].forEach(child => nested.appendChild(addToClassTree(tree, child, classPath + '/' + child)));
    parentElem.appendChild(nested);
  } else {
    parentElem.innerText = parent;
    parentElem.addEventListener('click', function() {
      const pageElement = document.getElementById('current-page');
      const qs = '/sql?' + updateQueryString('keyword', classPath) + '&' + updateQueryString('page', pageElement.innerText);
      removeOrgs();
      addTitle(classPath);
      addPagination();
      addOrgs(qs);
    });
  }
  return parentElem;
}

function setUpPrototype() {
  addListener();
  getClassifications();
}

function setUpDetailsPage() {
  addListener();
  getClassifications();
  loadOrg();
}

function loadOrg() {
  const url = new URL(window.location.href);
  const id = url.searchParams.get('id');
  console.log(id);
  const qs = '/sql?' + updateQueryString('id', id);
  // addOrgs(qs);
}
